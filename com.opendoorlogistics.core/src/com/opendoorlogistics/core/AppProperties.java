/*******************************************************************************
 * Copyright (c) 2014 Open Door Logistics (www.opendoorlogistics.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License 3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 ******************************************************************************/
package com.opendoorlogistics.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.opendoorlogistics.core.air21.EdfsStops;
import com.opendoorlogistics.core.utils.Numbers;
import com.opendoorlogistics.core.utils.PropertiesUtils;
import com.opendoorlogistics.core.utils.strings.Strings;

/**
 * Class for the app-wide properties
 * 
 * @author Phil
 *
 */
public class AppProperties {
	private static final Logger logger = Logger.getLogger(AppProperties.class.getName());

	private static Properties applicationProperties;

	static {
		applicationProperties = new Properties();
		loadEmbedded(applicationProperties);
		PropertiesUtils.loadFromFile(new File(AppConstants.ODL_EXTERNAL_PROPERTIES_FILE), applicationProperties);
		downloadFiles();
		for (Map.Entry<Object, Object> entry : applicationProperties.entrySet()) {
			logger.info("\t" + entry.getKey() + "=" + entry.getValue());
		}
	}

//	public static Properties get(){
//		return applicationProperties;
//	}

	public static final String SPATIAL_KEY = "spatial";

	public static final String SPATIAL_RENDERER_KEY = SPATIAL_KEY + ".renderer";

	public static final String SPATIAL_RENDERER_SIMPLIFY_DISTANCE_TOLERANCE = SPATIAL_RENDERER_KEY
			+ ".simplify_distance_tolerance";

	public static final String SPATIAL_RENDERER_SIMPLIFY_DISTANCE_TOLERANCE_LINESTRING = SPATIAL_RENDERER_SIMPLIFY_DISTANCE_TOLERANCE
			+ ".linestring";

	public static final String API_USER = "user";

	public static final String API_PASS = "pass123";

	public synchronized static Double getDouble(String key) {
		if (applicationProperties != null) {
			Object val = getValue(key);
			if (val != null) {
				return Numbers.toDouble(val);
			}
		}
		return null;
	}

	public synchronized static Object getValue(String key) {
		key = Strings.std(key);
		if (applicationProperties != null) {
			return applicationProperties.get(key);
		}
		return null;
	}

	public synchronized static Double getDouble(String key, double defaultValueIfKeyMissing) {
		Double ret = getDouble(key);
		if (ret == null) {
			ret = defaultValueIfKeyMissing;
		}
		return ret;
	}

	public synchronized static String getString(String key) {
		if (applicationProperties != null) {
			Object val = getValue(key);
			if (val != null) {
				return val.toString();
			}
		}
		return null;
	}

	private synchronized static void loadEmbedded(Properties addTo) {
		InputStream stream = null;
		try {
			// Use own class loader to prevent problems when jar loaded by reflection
			stream = AppProperties.class.getResourceAsStream(AppConstants.ODL_EMBEDED_PROPERTIES_FILE);
			Properties tmp = new Properties();
			tmp.load(stream);
			addTo(tmp, addTo);
			logger.info("Loaded embedded properties.");
		} catch (Exception e) {
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e2) {
				}
			}
		}
	}

	private synchronized static void addTo(Properties source, Properties addTo) {
		for (String key : source.stringPropertyNames()) {
			String std = Strings.std(key);
			Object val = source.get(key);
			addTo.put(std, val);
		}
	}

	public synchronized static void add(Properties properties) {
		if (applicationProperties == null) {
			applicationProperties = new Properties();
		}

		addTo(properties, applicationProperties);
	}

	public synchronized static void main(String[] args) {
		loadEmbedded(new Properties());
	}

	public synchronized static Boolean getBool(String key) {

		String s = getString(key);
		if (s != null) {
			if (Strings.equalsStd("true", s) || Strings.equalsStd("1", s)) {
				return true;
			}
			if (Strings.equalsStd("false", s)) {
				return false;
			}
		}
		return null;
	}

	public synchronized static Set<String> getKeys() {
		return applicationProperties.stringPropertyNames();

	}

	public synchronized static void put(String key, Object value) {
		applicationProperties.put(Strings.std(key), value);
	}

	// Create client API to Download Files over the net
	public synchronized static void downloadFiles() {
		System.out.println("Test downloadFiles .....");
		HttpGet request = new HttpGet("http://localhost:8088/odlapi/vehicleroutes/stops");

		String auth = API_USER + ":" + API_PASS;

		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
		String authHeader = "Basic " + new String(encodedAuth);

		request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(getSSLContext())
				.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

		HttpResponse response;
		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String result = "";
			List<EdfsStops> list = new ArrayList<>();
			if (entity != null) {

				InputStream inputStream = entity.getContent();
				result = convertStreamToString(inputStream);

				JSONArray jsonArray = new JSONArray(result);

				for (int i = 0; i < jsonArray.length(); i++) {
					System.out.println("Result from Client Pull... " + result);
					JSONObject myObj = jsonArray.getJSONObject(i);
					
					EdfsStops bean = new EdfsStops();
					bean.setAddress((String) myObj.get("address"));
					bean.setDimension((Double) myObj.get("dimension"));
					bean.setEndTime((String) myObj.get("endTime"));
					bean.setId((String) myObj.get("id"));
					bean.setJobId((String) myObj.get("jobId"));
					bean.setLatitude((String) myObj.get("latitude"));
					bean.setLongitude((String) myObj.get("longitude"));
					bean.setServiceDuration((String) myObj.get("serviceDuration"));
					bean.setName((String) myObj.get("name"));
					bean.setQuantity((Integer) myObj.get("quantity"));
					
					list.add(bean);
				}

			}
			
			System.out.println("List Size: " + list.size());

//			JSONObject myObj = new JSONObject(result);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static SSLContext getSSLContext() {
		SSLContext sSLContext = null;
		try {
			sSLContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return super.isTrusted(chain, authType); // To change body of generated methods, choose Tools |
																// Templates.
				}
			}).build();
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			return sSLContext;
		}
	}

	public static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		}
		return sb.toString();
	}

}
