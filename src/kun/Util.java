package kun;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;

public class Util {

	private static Canvas testCanvas;
	
	public static String url(String url) {
		StringBuffer sb = new StringBuffer();
		char[] chars = url.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int c = chars[i];
			if (65 <= c && c <= 90) {
				sb.append((char) c);
			} else if (97 <= c && c <= 122) {
				sb.append((char) c);
			} else if (48 <= c && c <= 57) {
				sb.append((char) c);
			} else if (c == 32) {
				sb.append("%20");
			} else if (c == 45 || c == 95 || c == 46 || c == 33 || c == 126 || c == 42 || c == 39 || c == 40
					|| c == 41) {
				sb.append((char) c);
			} else if (c <= 127) {
				sb.append(hex(c));
			} else if (c <= 2047) {
				sb.append(hex(0xC0 | c >> 6));
				sb.append(hex(0x80 | c & 0x3F));
			} else {
				sb.append(hex(0xE0 | c >> 12));
				sb.append(hex(0x80 | c >> 6 & 0x3F));
				sb.append(hex(0x80 | c & 0x3F));
			}
		}
		return sb.toString();
	}

	private static String hex(int i) {
		String s = Integer.toHexString(i);
		return "%".concat(s.length() < 2 ? "0" : "").concat(s);
	}
	
	public static String[] getStringArray(String text, int maxWidth, Font font) {
		if (text == null || text.length() == 0 || text.equals(" ") || maxWidth < font.charWidth('W') + 2) {
			return new String[0];
		}
		text = replace(text, "\r", "");
		Vector v = new Vector(3);
		char[] chars = text.toCharArray();
		if (text.indexOf('\n') > -1) {
			int j = 0;
			for (int i = 0; i < text.length(); i++) {
				if (chars[i] == '\n') {
					v.addElement(text.substring(j, i));
					j = i + 1;
				}
			}
			v.addElement(text.substring(j, text.length()));
		} else {
			v.addElement(text);
		}
		for (int i = 0; i < v.size(); i++) {
			String s = (String) v.elementAt(i); 
			if(font.stringWidth(s) >= maxWidth) {
				int i1 = 0;
				for (int i2 = 0; i2 < s.length(); i2++) {
					if (font.stringWidth(s.substring(i1, i2+1)) >= maxWidth) {
						boolean space = false;
						for (int j = i2; j > i1; j--) {
							char c = s.charAt(j);
							if (c == ' ' || (c >= ',' && c <= '/')) {
								space = true;
								v.setElementAt(s.substring(i1, j + 1), i);
								v.insertElementAt(s.substring(j + 1), i + 1);
								i += 1;
								i2 = i1 = j + 1;
								break;
							}
						}
						if (!space) {
							i2 = i2 - 2;
							v.setElementAt(s.substring(i1, i2), i);
							v.insertElementAt(s.substring(i2), i + 1);
							i2 = i1 = i2 + 1;
							i += 1;
						}
					}
				}
			}
		}
		String[] arr = new String[v.size()];
		v.copyInto(arr);
		return arr;
	}

	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static Canvas testCanvas() {
		if(testCanvas == null)
			testCanvas = new TestCanvas();
		KunUI.width = testCanvas.getWidth();
		KunUI.height = testCanvas.getHeight();
		return testCanvas;
	}
	
	public static float lerp(float start, float target, float mul, float div) {
		return start + ((target - start) * mul / div);
	}
	
	public static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + 1);
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}

	static void addDays(Calendar cal, int days) {
		cal.setTime(new Date(cal.getTime().getTime() + days * 86400000L));
	}
	
	static Calendar parseApiDate(String date) {
		Calendar cal = Calendar.getInstance();
		if(date.indexOf('T') != -1) {
			String[] dateSplit = Util.split(date.substring(0, date.indexOf('T')), '-');
			String[] timeSplit = Util.split(date.substring(date.indexOf('T')+1), ':');
			cal.set(Calendar.YEAR, Integer.parseInt(dateSplit[0]));
			cal.set(Calendar.MONTH, Integer.parseInt(dateSplit[1])-1);
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[2]));
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]));
			cal.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]));
			cal.set(Calendar.SECOND, Integer.parseInt(Util.split(timeSplit[2], '.')[0]));
		} else {
			String[] dateSplit = Util.split(date, '-');
			cal.set(Calendar.YEAR, Integer.parseInt(dateSplit[0]));
			cal.set(Calendar.MONTH, Integer.parseInt(dateSplit[1])-1);
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[2]));
		}
		return cal;
	}
	
	static String getApiDate(Calendar cal) {
		String month = Integer.toString(cal.get(Calendar.MONTH)+1);
		String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		if(month.length() < 2) {
			month = "0" + month;
		}
		if(day.length() < 2) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "-" + month + "-" + day;
	}
	
	static String getApiDateTime(Calendar cal) {
		String month = Integer.toString(cal.get(Calendar.MONTH)+1);
		String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		if(month.length() < 2) {
			month = "0" + month;
		}
		if(day.length() < 2) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "-" + month + "-" + day + "T" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
	}
	
	static String localizeDayOfWeek(int dayOfWeek) {
		switch(dayOfWeek) {
		case Calendar.SUNDAY:
			return "Воскресенье";
		case Calendar.MONDAY:
			return "Понедельник";
		case Calendar.TUESDAY:
			return "Вторник";
		case Calendar.WEDNESDAY:
			return "Среда";
		case Calendar.THURSDAY:
			return "Четверг";
		case Calendar.FRIDAY:
			return "Пятница";
		case Calendar.SATURDAY:
			return "Суббота";
		default:
			return "";
		}
	}
	
	static String localizeMonth(int month) {
		switch(month) {
		case Calendar.JANUARY:
			return "Январь";
		case Calendar.FEBRUARY:
			return "Февраль";
		case Calendar.MARCH:
			return "Март";
		case Calendar.APRIL:
			return "Апрель";
		case Calendar.MAY:
			return "Май";
		case Calendar.JUNE:
			return "Июнь";
		case Calendar.JULY:
			return "Июль";
		case Calendar.AUGUST:
			return "Август";
		case Calendar.SEPTEMBER:
			return "Сентябрь";
		case Calendar.OCTOBER:
			return "Октябрь";
		case Calendar.NOVEMBER:
			return "Ноябрь";
		case Calendar.DECEMBER:
			return "Декабрь";
		default:
			return "";
		}
	}
	
	static String localizeMonthWithCase(int month) {
		switch(month) {
		case Calendar.JANUARY:
			return "января";
		case Calendar.FEBRUARY:
			return "февраля";
		case Calendar.MARCH:
			return "марта";
		case Calendar.APRIL:
			return "апреля";
		case Calendar.MAY:
			return "мая";
		case Calendar.JUNE:
			return "июня";
		case Calendar.JULY:
			return "июля";
		case Calendar.AUGUST:
			return "августа";
		case Calendar.SEPTEMBER:
			return "сентября";
		case Calendar.OCTOBER:
			return "октября";
		case Calendar.NOVEMBER:
			return "ноября";
		case Calendar.DECEMBER:
			return "декабря";
		default:
			return "";
		}
	}

	public static String localizeDateWithWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		if(date != null) cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH) + " " + localizeMonthWithCase(cal.get(Calendar.MONTH)) + ", " + localizeDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)).toLowerCase();
	}

	public static String localizeDate(Date date) {
		Calendar cal = Calendar.getInstance();
		if(date != null) cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH) + " " + localizeMonthWithCase(cal.get(Calendar.MONTH));
	}

	public static String localizeToday() {
		return localizeDateWithWeek(null);
	}

	public static String oneLine(String text, Font font, int maxWidth) {
		text = replace(text, "\r", "").replace('\n', ' ');
		if(font.stringWidth(text) < maxWidth) {
			return text;
		}
		while(font.stringWidth(text + "..") >= maxWidth) {
			text = text.substring(0, text.length() - 1);
		}
		return text + "..";
	}

	public static boolean isDateEqual(Date a, Date b) {
		if(a == null || b == null) return false;
		Calendar cala = Calendar.getInstance();
		cala.setTime(a);
		Calendar calb = Calendar.getInstance();
		calb.setTime(b);
		return cala.get(Calendar.YEAR) == calb.get(Calendar.YEAR) && 
				cala.get(Calendar.MONTH) == calb.get(Calendar.MONTH) && 
				cala.get(Calendar.DAY_OF_MONTH) == calb.get(Calendar.DAY_OF_MONTH);
	}
}
