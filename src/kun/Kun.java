package kun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.RecordStore;

import cc.nnproject.json.AbstractJSON;
import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class Kun {
	static final String CONFIG_RECORD_NAME = "kunconfig";
	static final String kundelikApiUrl = "https://api.kundelik.kz/v2/";
	static final String dnevnikApiUrl = "https://api.dnevnik.ru/v2/";
	static final String proxy = "http://nnp.nnchan.ru/pproxy.php?u=";
	static Kun inst;
	static String version;
	static KunMID midlet;
	static KunUI ui;
	static boolean running;
	static String accessToken;
	static String refreshToken;
	static String user;
	static String personId;
	static String shortName;
	static String groupId;
	static JSONArray schools;
	static JSONArray eduGroups;
	static Hashtable subjectsTable = new Hashtable();
	static boolean dnevnik = false;
	static String api = dnevnik ? dnevnikApiUrl : kundelikApiUrl;

	protected static void startApp() {
		running = true;
		inst = new Kun();
		if (version == null)
			version = midlet.getAppProperty("MIDlet-Version");
		loadConfig();
		ui = new KunUI();
		ui.init();
		ui.postInit();
	}

	static void loadConfig() {
		RecordStore r = null;
		try {
			r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, false);
		} catch (Exception e) {
		}
		if (r != null) {
			try {
				JSONObject j = JSON.getObject(new String(r.getRecord(1), "UTF-8"));
				if (j.has("accessToken"))
					accessToken = j.getString("accessToken");
				if (j.has("refreshToken"))
					refreshToken = j.getString("refreshToken");
				if (j.has("user"))
					user = j.getString("user");
				r.closeRecordStore();
			} catch (Exception e) {
			}
		}
	}

	static void saveConfig() {
		try {
			RecordStore.deleteRecordStore(CONFIG_RECORD_NAME);
		} catch (Exception e) {
		}
		try {
			RecordStore r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, true);
			JSONObject j = new JSONObject();
			j.put("accessToken", accessToken);
			j.put("refreshToken", refreshToken);
			j.put("user", user);
			byte[] b = j.build().getBytes("UTF-8");
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
		}
	}

	static void loggedInit() {
		if(personId != null) return;
		saveConfig();
		try {
			getUserContext();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Get user context failed: " + e.toString());
		}
	}

	static String auth(String login, String password) throws Exception {
		JSONObject params = new JSONObject();
		params.put("client_id", "387D44E3-E0C9-4265-A9E4-A4CAAAD5111C");
		params.put("client_secret", "8A7D709C-FDBB-4047-B0EA-8947AFE89D67");
		params.put("scope", "Schools,Relatives,EduGroups,Lessons,marks,EduWorks,Avatar,EducationalInfo,CommonInfo,ContactInfo,FriendsAndRelatives,Files,Wall,Messages");
		params.put("username", login);
		params.put("password", password);
		String param = params.build();
		try {
			JSONObject resJson = (JSONObject) apiPost("authorizations/bycredentials", param);
			if (!resJson.has("accessToken")) {
				throw new Exception(resJson.build());
			}
			user = resJson.getString("user_str");
			refreshToken = resJson.getString("refreshToken");
			return accessToken = resJson.getString("accessToken");
		} catch (IOException e) {
			throw e;
		}
	}

	static JSONArray getCurrentWeekSchedule(String personId, String groupId) throws IOException {
		Calendar cal = Calendar.getInstance();
		int weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (weekDay == 0) {
			weekDay = 7;
		}
		Util.addDays(cal, -weekDay + 1);
		String startDate = Util.getApiDate(cal);
		Util.addDays(cal, 6);
		String endDate = Util.getApiDate(cal);
		return getSchedule(personId, groupId, startDate, endDate);
	}

	static JSONArray getNextWeekSchedule(String personId, String groupId) throws IOException {
		Calendar cal = Calendar.getInstance();
		int weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (weekDay == 0) {
			weekDay = 7;
		}
		Util.addDays(cal, -weekDay + 1 + 7);
		String startDate = Util.getApiDate(cal);
		Util.addDays(cal, 6);
		String endDate = Util.getApiDate(cal);
		return getSchedule(personId, groupId, startDate, endDate);
	}

	static JSONArray getSchedule(String personId, String groupId, String date) throws IOException {
		return getSchedule(personId, groupId, date, date);
	}

	static JSONArray getSchedule(String personId, String groupId, String startDate, String endDate)
			throws IOException {
		JSONArray res = new JSONArray();
		JSONObject json = (JSONObject) apiGet("persons/" + personId + "/groups/" + groupId + "/schedules?startDate="
				+ startDate + "&endDate=" + endDate);

		JSONArray days = json.getArray("days");
		for (Enumeration e = days.elements(); e.hasMoreElements();) {
			JSONObject resDay = new JSONObject();
			res.add(resDay);
			JSONObject day = (JSONObject) e.nextElement();
			//System.out.println(day.format());
			String date = day.getString("date");
			if (date.indexOf('T') != -1) {
				date = date.substring(0, date.indexOf('T'));
			}
			resDay.put("date", date);
			JSONArray resLessons = new JSONArray();
			resDay.put("lessons", resLessons);
			JSONArray lessons = day.getArray("lessons");
			if (lessons.size() == 0) {
				continue;
			}
			JSONArray marks = day.getArray("marks");
			JSONArray homeworks = day.getArray("homeworks");
			JSONArray subjects = day.getArray("subjects");
			for (Enumeration e2 = lessons.elements(); e2.hasMoreElements();) {
				JSONObject resLesson = new JSONObject();
				JSONObject lesson = (JSONObject) e2.nextElement();
				JSONObject subject = null;
				JSONObject mark = null;
				JSONArray resHomeworks = new JSONArray();
				// Поиск сабжа по иду
				for (Enumeration e3 = subjects.elements(); e3.hasMoreElements();) {
					JSONObject tmpSubject = (JSONObject) e3.nextElement();
					String subjectId = lesson.getString("subjectId");
					if (!subjectsTable.containsKey(subjectId)) {
						subjectsTable.put(subjectId, tmpSubject);
					}
					if (tmpSubject.getString("id").equalsIgnoreCase(subjectId)) {
						subject = tmpSubject;
						break;
					}
				}
				// Оценка
				for (Enumeration e3 = marks.elements(); e3.hasMoreElements();) {
					JSONObject tmpMark = (JSONObject) e3.nextElement();
					if (tmpMark.getString("lesson").equalsIgnoreCase(lesson.getString("id"))) {
						mark = tmpMark;
						break;
					}
				}
				// Домашка
				for (Enumeration e3 = homeworks.elements(); e3.hasMoreElements();) {
					JSONObject tmpHomework = (JSONObject) e3.nextElement();
					if (tmpHomework.getString("lesson").equalsIgnoreCase(lesson.getString("id"))) {
						resHomeworks.add(tmpHomework.getString("text"));
					}
				}
				int num = lesson.getInt("number");
				resLesson.put("number", new Integer(num));
				resLesson.put("lessonId", new Integer(lesson.getInt("id")));
				resLesson.put("subjectId", new Integer(lesson.getInt("subjectId")));
				resLesson.put("name", subject.getString("name"));
				resLesson.put("title", lesson.getString("title"));
				resLesson.put("hours", lesson.getString("hours"));
				if (mark != null) {
					JSONObject resMark = new JSONObject();
					resMark.put("type", mark.getString("type"));
					resMark.put("value", mark.getString("textValue"));
					resMark.put("mood", mark.getString("mood"));
					resLesson.put("mark", resMark);
				}
				resLesson.put("homeworks", resHomeworks);
				// Сортировка уроков по порядку
				if (resLessons.size() > 0) {
					int i = 0;
					while (i < resLessons.size()) {
						if (num > resLessons.getObject(i).getInt("number")) {
							i++;
						} else {
							break;
						}
					}
					resLessons.put(i, resLesson);
				} else
					resLessons.add(resLesson);
			}
		}
		return res;
	}

	private static JSONObject getUserContext() throws IOException {
		JSONObject json = (JSONObject) apiGet("users/me/context");
		personId = json.getString("personId");
		shortName = json.getString("shortName");
		groupId = json.getArray("groupIds").getString(0);
		schools = json.getArray("schools");
		eduGroups = json.getArray("eduGroups");
		return json;
	}

	static AbstractJSON apiGet(String url) throws IOException {
		//System.out.println("GET " + url);
		String r = getUtf(proxy + Util.url("https://api.dnevnik.ru/v2/" + url));
		AbstractJSON json;
		switch (r.charAt(0)) {
		case '{':
			json = JSON.getObject(r);
			JSONObject obj = (JSONObject) json;
			if (obj.has("type") && obj.getString("type").indexOf("invalidToken") != -1) {
				throw new RuntimeException("Invalid token!");
			}
			break;
		case '[':
			json = JSON.getArray(r);
			break;
		default:
			throw new RuntimeException("API response not in JSON!\n\n" + r);
		}
		return json;
	}

	static AbstractJSON apiPost(String url, String data) throws IOException {
		//System.out.println("POST " + url);
		String r = postUtf(proxy + Util.url(api + url) + "&post", data);
		AbstractJSON json;
		switch (r.charAt(0)) {
		case '{':
			json = JSON.getObject(r);
			break;
		case '[':
			json = JSON.getArray(r);
			break;
		default:
			throw new RuntimeException("API response not in JSON!\n\n" + r);
		}
		return json;
	}

	static ContentConnection open(String url) throws IOException {
		try {
			ContentConnection con = (ContentConnection) Connector.open(url);
			if (con instanceof HttpConnection && accessToken != null
					&& (url.toString().indexOf("api.kundelik.kz") != -1 || url.toString().indexOf("api.dnevnik.ru") != -1)) {
				((HttpConnection) con).setRequestProperty("Access-Token", accessToken);
			}
			return con;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	static byte[] bytes(String url) throws IOException {
		System.out.println("GET " + url);
		HttpConnection hc = (HttpConnection) open(url);
		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("GET");
			int r = hc.getResponseCode();
			if (r != 200)
				throw new IOException(r + " " + hc.getResponseMessage());
			is = hc.openInputStream();
			o = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
				o.write(buf, 0, len);
			}
			return o.toByteArray();
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new IOException(e.toString());
		} finally {
			if (is != null)
				is.close();
			if (hc != null)
				hc.close();
			if (o != null)
				o.close();
		}
	}

	static String getUtf(String url) throws IOException {
		byte[] b = bytes(url);
		try {
			return new String(b, "UTF-8");
		} catch (Throwable e) {
			return new String(b);
		}
	}

	static String postUtf(String url, String data) throws IOException {
		return new String(postBytes(url, data), "UTF-8");
	}

	static byte[] postBytes(String url, String data) throws IOException {
		byte[] b = data.getBytes("UTF-8");
		System.out.println("GET " + url);
		HttpConnection hc = (HttpConnection) open(url);
		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("GET");
			hc.setRequestProperty("Content-Length", "" + b.length);
			OutputStream os = hc.openOutputStream();
			os.write(b);
			os.close();
			int r = hc.getResponseCode();
			if (r != 200)
				throw new IOException(r + " " + hc.getResponseMessage());
			is = hc.openInputStream();
			o = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
				o.write(buf, 0, len);
			}
			return o.toByteArray();
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new IOException(e.toString());
		} finally {
			if (is != null)
				is.close();
			if (hc != null)
				hc.close();
			if (o != null)
				o.close();
		}
	}

}
