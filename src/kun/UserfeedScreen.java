package kun;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class UserfeedScreen extends UIScreen {

	private Image kunImg;
	private Image goodImg;
	private Image avgImg;
	private Image badImg;
	private Image leftArrowImg;
	private Image rightArrowImg;

	private int screenHeight;
	private int scrollTarget;
	private int scrollTimer;

	private String className;
	private String schoolName;
	private String scheduleDate;
	
	private int dayOffset;
	private int arrowY;

	private Object[][] lessons;
	private Object[][] marks;
	private boolean loading;

	UserfeedScreen() {
		super(null, null);
		try {
			kunImg = Image.createImage("/kz36.png");
			goodImg = Image.createImage("/good.png");
			avgImg = Image.createImage("/avg.png");
			badImg = Image.createImage("/bad.png");
			leftArrowImg = Image.createImage("/leftarrow.png");
			rightArrowImg = Image.createImage("/rightarrow.png");
		} catch (Exception e) {
		}
		loadSchedule();
		try {
			//Calendar cal = Calendar.getInstance();
			//Util.addDays(cal, -30);
			//marks = (JSONArray) Kun.apiGet("persons/" + Kun.personId + "/edu-groups/" + Kun.groupId + "/marks/" + cal + "/" + Util.getApiDate(Calendar.getInstance()));
			JSONObject recentMarks = (JSONObject) Kun.apiGet("persons/" + Kun.personId + "/group/" + Kun.groupId + "/recentmarks?limit=10");
			for(Enumeration e3 = recentMarks.getArray("subjects").elements(); e3.hasMoreElements();) {
				JSONObject tmpSubject = (JSONObject) e3.nextElement();
				String subjectId = tmpSubject.getString("id");
				if(!Kun.subjectsTable.containsKey(subjectId)) {
					Kun.subjectsTable.put(subjectId, tmpSubject);
				}
			}
			// парс последних оценок
			JSONArray marks = recentMarks.getArray("marks");
			Date lastDate = null;
			this.marks = new Object[marks.size()][];
			for(int i = 0; i < marks.size(); i++) {
				JSONObject mark = marks.getObject(i);
				this.marks[i] = new Object[4];
				Date date = Util.parseApiDate(mark.getString("date")).getTime();
				if(lastDate == null || !Util.isDateEqual(date, lastDate)) {
					lastDate = date;
					this.marks[i][0] = Util.localizeDateShort(date);
				}
				String mood = mark.getString("mood");
				this.marks[i][1] = "good".equalsIgnoreCase(mood) ? goodImg : "bad".equalsIgnoreCase(mood) ? badImg : avgImg;
				this.marks[i][2] = mark.getString("value");
				JSONObject subj = ((JSONObject)Kun.subjectsTable.get(mark.getString("lesson_str")));
				if(subj != null) {
					this.marks[i][3] = subj.getString("name");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		className = Kun.eduGroups.getObject(0).getString("name");
		schoolName = Kun.schools.getObject(0).getString("name");
	}
	
	private void loadSchedule() {
		try {
			loading = true;
			repaint();
			Calendar cal = Calendar.getInstance();
			Util.addDays(cal, dayOffset);
			scheduleDate = Util.localizeDateWithWeek(cal.getTime());
			JSONArray lessons = Kun.getSchedule(Kun.personId, Kun.groupId, Util.getApiDate(cal)).getObject(0).getArray("lessons");
			this.lessons = new Object[lessons.size()][];
			for(int i = 0; i < lessons.size(); i++) {
				JSONObject lesson = lessons.getObject(i);
				JSONArray homeworks = lesson.getArray("homeworks");
				this.lessons[i] = new Object[7];
				this.lessons[i][0] = String.valueOf(lesson.getInt("number"));
				this.lessons[i][1] = lesson.getString("hours");
				this.lessons[i][2] = lesson.getString("name");
				if(lesson.has("mark")) {
					JSONObject mark = lesson.getObject("mark");
					this.lessons[i][3] = "good".equalsIgnoreCase(mark.getString("mood")) ? Boolean.TRUE : Boolean.FALSE;
					this.lessons[i][4] = mark.getString("value");
				}
				this.lessons[i][5] = lesson.getString("title");
				if(homeworks.size() > 0) {
					this.lessons[i][6] = homeworks.getString(0);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		loading = false;
		repaint();
	}
	
	protected void tap(int x, int y, int time) {
		if(y < 44 && x > KunUI.width - 60) {
			Kun.midlet.notifyDestroyed();
		} else if(y > arrowY+scroll && y < arrowY+scroll+50) {
			if(x < 50) {
				dayOffset--;
				loadSchedule();
			} else if(x > KunUI.width - 50) {
				dayOffset++;
				loadSchedule();
			}
		}
	}

	protected void paint(Graphics g, int w, int h) {
		g.setColor(-1);
		g.fillRect(0, 0, w, h);
		g.setClip(0, 0, w, 44);
		g.drawImage(kunImg, 4, 4, 0);
		if(!ui.keyInput) {
			g.setFont(smallfont);
			g.setColor(0x5f5f5f);
			g.drawString("Выход", w - 8, (44 - smallfontheight) >> 1, Graphics.RIGHT | Graphics.TOP);
		}
		screenHeight = h;
		
		if(scroll < -height + screenHeight && scroll != 0 && !ui.scrolling) {
			scroll = -height + screenHeight;
		}
		if(scroll > 0) {
			scroll = 0;
		}
		g.setClip(0, 44, w, h-44);
		g.translate(0, (int)scroll);
		
		g.setColor(0xF6FAFC);
		int nameh = Math.max(48, 10+mediumboldfontheight+smallfontheight);
		g.fillRect(0, 44, w, nameh);
		
		g.setColor(0x0273B2);
		g.setFont(mediumboldfont);
		g.drawString(Kun.shortName, 4, 44+4, 0);
		g.setColor(0x7f7f7f);
		g.setFont(smallfont);
		g.drawString(className + ", " + schoolName, 4, 44+6+mediumboldfontheight, 0);
		g.setColor(0xEBF1F6);
		int yy = 44+nameh;
		int markh = Math.min(100, 51+(smallfontheight*3)+8);
		g.fillRect(0, yy, w, markh);
		if(marks.length == 0) {
			g.setColor(0);
			g.drawString("Нет оценок", w >> 1, yy + ((64 - mediumfontheight) >> 1), Graphics.HCENTER | Graphics.TOP);
		} else if(yy+markh+scroll > 0) {
			int xx = 5;
			for(int i = 0; i < marks.length; i++) {
				if(xx > w) break;
				Object[] mark = marks[i];
				if(mark[0] != null) {
					g.setColor(0x2C2C2C);
					g.setFont(smallfont);
					g.drawString((String) mark[0], xx, yy + 2, 0);
				}
				g.drawImage((Image) mark[1], xx, yy  + smallfontheight + 4, 0);
				g.setColor(-1);
				g.setFont(largefont);
				g.drawString((String) mark[2], xx + 47, yy + smallfontheight + 4 + ((50 - largefontheight) >> 1), Graphics.HCENTER | Graphics.TOP);
				g.setColor(0);
				g.setFont(smallfont);
				if(mark[3] != null) {
					g.drawString((String) mark[3], xx, yy + smallfontheight + largefontheight + 8, 0);
				}
				xx += 100;
			}
		}
		yy += markh;
		g.setColor(0xD8D8D8);
		g.drawLine(0, yy, w, yy);
		yy++;
		g.setColor(0);
		g.setFont(mediumfont);
		g.drawString(scheduleDate, w >> 1, yy + ((48 - mediumfontheight) >> 1), Graphics.HCENTER | Graphics.TOP);
		arrowY = yy;
		g.drawImage(leftArrowImg, 8, yy + 24, Graphics.LEFT | Graphics.VCENTER);
		g.drawImage(rightArrowImg, w - 8, yy + 24, Graphics.RIGHT | Graphics.VCENTER);
		yy+=48;
		g.setColor(0xD8D8D8);
		g.drawLine(0, yy, w, yy);
		if(loading || lessons.length == 0) {
			g.drawRect(0, yy, w, 64);
			g.setColor(0);
			g.drawString(loading ? "Загрузка" : "В этот день нет уроков", w >> 1, yy + ((64 - mediumfontheight) >> 1), Graphics.HCENTER | Graphics.TOP);
			yy+=64;
		} else {
			for(int i = 0; i < lessons.length; i++) {
				Object[] lesson = lessons[i];
				g.setFont(smallfont);
				g.setColor(0x7f7f7f);
				g.drawString(((String) lesson[0]).concat(" УРОК"), 8, yy+8, 0);
				g.setColor(0x0273B2);
				g.drawString((String) lesson[1], w - 12, yy+8, Graphics.RIGHT | Graphics.TOP);
				yy += 8 + smallfontheight;
				g.setFont(mediumboldfont);
				g.setColor(0);
				g.drawString((String) lesson[2], 8, yy+8, 0);
				if(lesson[3] != null) {
					g.setColor(Boolean.TRUE.equals(lesson[3]) ? 0xC2D23A : 0xF9A23B);
					g.fillRect(w-42, yy, 30, 30);
					g.setColor(-1);
					g.setFont(smallfont);
					g.drawString((String) lesson[4], w-42+15, yy + ((30 - smallfontheight) >> 1), Graphics.HCENTER | Graphics.TOP);
				}
				yy += 8 + mediumboldfontheight;
				g.setColor(0x7f7f7f);
				g.setFont(smallfont);
				g.drawString(Util.getOneLine((String) lesson[5], smallfont, w-16), 8, yy+4, 0);
				yy += 4 + smallfontheight;
				g.setColor(0);
				if(lesson[6] != null) {
					g.drawString(Util.getOneLine((String) lesson[6], smallfont, w-16), 8, yy+8, 0);
				}
				yy += 16 + smallfontheight;
				g.setColor(0xD8D8D8);
				g.drawLine(0, yy, w, yy);
			}
		}
		
		height = yy;

		g.translate(0, -g.getTranslateY());
		
		if(scrollTimer < 2 && height > 0 && height > screenHeight) {
			scrollTimer++;
			//g.setColor(AppUI.getColor(COLOR_SCROLLBAR_FG));
			g.setColor(0xadadad);
			int sw = 4;
			int hh = height;
			if(hh <= 0) hh = 1;
			int sby = (int)(((float)-scroll / (float)hh) * h);
			int sbh = (int)(((float)h / (float)hh) * h);
			g.fillRect(w-sw, sby, sw-2, sbh);
		}
		if(scrollTarget <= 0) {
			scrollTimer = 0;
			ui.scrolling = true;
			if (Math.abs(scroll - scrollTarget) < 1) {
				scroll = scrollTarget;
				scrollTarget = 1;
				ui.scrolling = false;
			} else {
				scroll = Util.lerp(scroll, scrollTarget, 4, 20);
			}
			if(scroll > 0) {
				scroll = 0;
				scrollTarget = 1;
				ui.scrolling = false;
			}
			if(scroll < -height + screenHeight) {
				scroll = -height + screenHeight;
				scrollTarget = 1;
				ui.scrolling = false;
			}
		}
	}
	
	protected boolean scroll(int units) {
		if(height == 0 || height <= screenHeight) {
			scroll = 0;
			return false;
		}
		if(scroll + units < -height + screenHeight) {
			scroll = -height + screenHeight;
			return false;
		}
		if(units == 0) return false;
		scroll += units;
		if (scroll > 0) {
			scroll = 0;
			return false;
		}
		scrollTimer = 0;
		scrollTarget = 1;
		return true;
	}

}
