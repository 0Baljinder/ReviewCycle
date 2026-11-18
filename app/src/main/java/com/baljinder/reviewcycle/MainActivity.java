package com.baljinder.reviewcycle;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int BG=Color.rgb(247,248,252), INK=Color.rgb(29,35,55), MUTED=Color.rgb(111,119,143), BLUE=Color.rgb(82,103,216), GREEN=Color.rgb(31,145,105), RED=Color.rgb(190,73,73), WHITE=Color.WHITE;
    private DB db; private LinearLayout root, content, nav; private String screen="home"; private long selectedSubject=-1; private String selectedSubjectName="";
    private final SimpleDateFormat dateFmt=new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat shortFmt=new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR); db=new DB(this); render();}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void pad(View v,int l,int t,int r,int b){v.setPadding(dp(l),dp(t),dp(r),dp(b));}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private void gap(LinearLayout parent,int h){View v=new View(this);parent.addView(v,lp(1,h));}
    private TextView button(String label,int bg,int fg){TextView b=text(label,14,fg,true);b.setGravity(Gravity.CENTER);pad(b,15,12,15,12);b.setBackground(shape(bg,13));return b;}

    private void render(){
        root=column();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=column();pad(header,20,18,20,12);
        LinearLayout top=row();TextView brand=text("REVIEW CYCLE",12,BLUE,true);brand.setLetterSpacing(.12f);top.addView(brand,new LinearLayout.LayoutParams(0,dp(24),1));TextView setting=text("⚙",23,INK,true);setting.setGravity(Gravity.CENTER);setting.setOnClickListener(v->{screen="settings";render();});top.addView(setting,lp(42,40));header.addView(top);
        root.addView(header);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);content=column();pad(content,20,8,20,24);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav=row();pad(nav,12,8,12,10);nav.setBackgroundColor(WHITE);root.addView(nav,lp(-1,64));
        if(screen.equals("home"))home();else if(screen.equals("subjects"))subjects();else if(screen.equals("subject"))subjectTopics();else if(screen.equals("completed"))completed();else settings();
        navItem("Today","⌂","home");navItem("Subjects","▦","subjects");navItem("Completed","✓","completed");navItem("Settings","⚙","settings");
    }
    private void navItem(String label,String icon,String target){LinearLayout item=column();item.setGravity(Gravity.CENTER);TextView i=text(icon,19,screen.equals(target)|| (target.equals("subjects")&&screen.equals("subject"))?BLUE:MUTED,true);TextView l=text(label,10,screen.equals(target)|| (target.equals("subjects")&&screen.equals("subject"))?BLUE:MUTED,true);item.addView(i);item.addView(l);item.setOnClickListener(v->{screen=target;render();});nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));}
    private void heading(String title,String subtitle){content.addView(text(title,26,INK,true));if(subtitle!=null){gap(content,5);content.addView(text(subtitle,13,MUTED,false));}gap(content,18);}
    private void section(String title,String action,Runnable click){LinearLayout r=row();r.addView(text(title,17,INK,true),new LinearLayout.LayoutParams(0,dp(30),1));if(action!=null){TextView a=text(action,13,BLUE,true);r.addView(a);a.setOnClickListener(v->click.run());}content.addView(r);gap(content,10);}
    private void card(LinearLayout parent,View child){LinearLayout wrap=column();pad(wrap,15,14,15,14);wrap.setBackground(shape(WHITE,17));wrap.addView(child);LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(10);parent.addView(wrap,p);}
    private long today(){Calendar c=Calendar.getInstance();zero(c);return c.getTimeInMillis();}
    private void zero(Calendar c){c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);}
    private long addDays(long base,int days){Calendar c=Calendar.getInstance();c.setTimeInMillis(base);c.add(Calendar.DAY_OF_YEAR,days);return c.getTimeInMillis();}
    private String date(long ms){return shortFmt.format(new Date(ms));}
    private int count(String sql){Cursor c=db.getReadableDatabase().rawQuery(sql,null);int n=c.moveToFirst()?c.getInt(0):0;c.close();return n;}

    private void home(){
        heading("Your review desk","Small, consistent reviews make concepts stick.");
        int due=count("SELECT COUNT(*) FROM topics WHERE completed=0 AND next_review<="+today());int total=count("SELECT COUNT(*) FROM topics WHERE completed=0");int overdue=count("SELECT COUNT(*) FROM topics WHERE completed=0 AND next_review<"+today());
        LinearLayout hero=column();pad(hero,18,18,18,18);hero.setBackground(shape(Color.rgb(31,42,83),20));hero.addView(text("TODAY'S REVIEWS",11,Color.rgb(196,205,255),true));gap(hero,8);hero.addView(text(""+due,38,WHITE,true));hero.addView(text(due==1?"topic ready to review":"topics ready to review",14,Color.rgb(224,228,250),false));gap(hero,13);LinearLayout stats=row();stats.addView(text(total+" active topics",12,WHITE,true));View spacer=new View(this);stats.addView(spacer,new LinearLayout.LayoutParams(0,1,1));stats.addView(text(overdue+" overdue",12,Color.rgb(255,202,180),true));hero.addView(stats);card(content,hero);
        section("Due now",null,null);List<Topic> dueTopics=db.getTopics("completed=0 AND next_review<="+today(),"next_review ASC, title COLLATE NOCASE ASC");
        if(dueTopics.isEmpty())empty("You're all caught up. No reviews are due today.");else for(Topic t:dueTopics)topicCard(t,true);
        gap(content,7);section("Your subjects","See all",()->{screen="subjects";render();});List<Subject> ss=db.getSubjects();if(ss.isEmpty())empty("Create your first subject to organise your learning.");else for(Subject s:ss)subjectCard(s);
        TextView add=button("+  Add a subject",BLUE,WHITE);content.addView(add,lp(-1,48));add.setOnClickListener(v->addSubject());
    }
    private void subjects(){heading("Your subjects","Keep concepts organised by what you're studying.");TextView add=button("+  Create subject",BLUE,WHITE);content.addView(add,lp(-1,48));add.setOnClickListener(v->addSubject());gap(content,18);List<Subject> ss=db.getSubjects();if(ss.isEmpty())empty("No subjects yet. Add one to get started.");for(Subject s:ss)subjectCard(s);}
    private void subjectCard(Subject s){
        LinearLayout box=column();LinearLayout top=row();top.addView(text(s.name,16,INK,true),new LinearLayout.LayoutParams(0,dp(25),1));top.addView(text("›",25,MUTED,false));box.addView(top);int active=count("SELECT COUNT(*) FROM topics WHERE subject_id="+s.id+" AND completed=0");int due=count("SELECT COUNT(*) FROM topics WHERE subject_id="+s.id+" AND completed=0 AND next_review<="+today());gap(box,4);box.addView(text(active+" active topics  ·  "+due+" due",12,MUTED,false));card(content,box);box.setOnClickListener(v->{selectedSubject=s.id;selectedSubjectName=s.name;screen="subject";render();});}
    private void subjectTopics(){
        TextView back=text("‹  All subjects",13,BLUE,true);content.addView(back);back.setOnClickListener(v->{screen="subjects";render();});gap(content,12);heading(selectedSubjectName,"Topics are sorted from the next review date to the latest.");TextView add=button("+  Add topic",BLUE,WHITE);content.addView(add,lp(-1,46));add.setOnClickListener(v->addTopic());gap(content,16);
        List<Topic> topics=db.getTopics("subject_id="+selectedSubject+" AND completed=0","next_review ASC, title COLLATE NOCASE ASC");if(topics.isEmpty())empty("No active topics in this subject yet.");for(Topic t:topics)topicCard(t,false);
        int done=count("SELECT COUNT(*) FROM topics WHERE subject_id="+selectedSubject+" AND completed=1");if(done>0){gap(content,8);content.addView(text(done+" completed topic(s) — see Completed",12,GREEN,true));}
    }
    private void completed(){heading("Completed cycles","Topics that have finished all five review stages.");List<Topic> list=db.getTopics("completed=1","completed_at DESC, title COLLATE NOCASE ASC");if(list.isEmpty())empty("Completed topics will appear here after the 3, 5, 7, 15 and 30 day cycle.");for(Topic t:list){LinearLayout box=column();box.addView(text(t.title,16,INK,true));gap(box,5);box.addView(text(t.subject+"  ·  Finished "+date(t.completedAt),12,MUTED,false));card(content,box);}}
    private void topicCard(Topic t,boolean showSubject){
        LinearLayout box=column();LinearLayout top=row();top.addView(text(t.title,16,INK,true),new LinearLayout.LayoutParams(0,-2,1));if(t.overdue)top.addView(text("OVERDUE",10,RED,true));else if(t.dueToday)top.addView(text("DUE TODAY",10,BLUE,true));box.addView(top);gap(box,5);String sub=(showSubject?t.subject+"  ·  ":"")+"Stage "+(t.stage+1)+" of 5  ·  Next review: "+date(t.nextReview);box.addView(text(sub,12,MUTED,false));gap(box,10);LinearLayout actions=row();TextView progress=text(stageText(t.stage),11,BLUE,true);actions.addView(progress,new LinearLayout.LayoutParams(0,dp(34),1));TextView review=button("Mark reviewed",Color.rgb(232,237,255),BLUE);actions.addView(review);box.addView(actions);review.setOnClickListener(v->markReviewed(t));card(content,box);
    }
    private String stageText(int stage){String[] a={"Next: 3 days","Next: 5 days","Next: 7 days","Next: 15 days","Next: 30 days"};return a[Math.min(stage,4)];}
    private void empty(String message){LinearLayout b=column();b.setGravity(Gravity.CENTER);pad(b,18,22,18,22);b.setBackground(shape(WHITE,16));TextView t=text(message,13,MUTED,false);t.setGravity(Gravity.CENTER);b.addView(t);content.addView(b,lp(-1,-2));}

    private void addSubject(){EditText input=new EditText(this);input.setSingleLine(true);input.setHint("e.g. React, JavaScript, Cloud Computing");new AlertDialog.Builder(this).setTitle("New subject").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String n=input.getText().toString().trim();if(!n.isEmpty()){db.addSubject(n);render();}}).show();}
    private void addTopic(){EditText input=new EditText(this);input.setSingleLine(true);input.setHint("e.g. useEffect, Closures, DNS");new AlertDialog.Builder(this).setTitle("Add topic to "+selectedSubjectName).setMessage("The first review will be scheduled 3 days from today.").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Add topic",(d,w)->{String n=input.getText().toString().trim();if(!n.isEmpty()){db.addTopic(selectedSubject,n,today(),addDays(today(),3));render();}}).show();}
    private void markReviewed(Topic t){
        String msg="Mark ‘"+t.title+"’ as reviewed today? The next interval will be counted from today, even if this review was late.";
        new AlertDialog.Builder(this).setTitle("Complete review?").setMessage(msg).setNegativeButton("Not yet",null).setPositiveButton("Yes, reviewed",(d,w)->{
            long now=today();if(t.stage>=4){db.completeTopic(t.id,now);Toast.makeText(this,"Five review stages completed!",Toast.LENGTH_LONG).show();}else{int nextStage=t.stage+1;int[] intervals={3,5,7,15,30};db.advanceTopic(t.id,nextStage,addDays(now,intervals[nextStage]),now);Toast.makeText(this,"Next review: "+date(addDays(now,intervals[nextStage])),Toast.LENGTH_SHORT).show();}render();
        }).show();
    }

    private void settings(){heading("Settings","Your data stays on this device.");LinearLayout usage=column();usage.addView(text("STORAGE USED BY THIS APP",11,MUTED,true));gap(usage,8);long bytes=db.storageBytes();usage.addView(text(formatBytes(bytes),28,INK,true));gap(usage,4);usage.addView(text("Approximate size of your saved review database. Android may report additional app storage for the app itself.",12,MUTED,false));card(content,usage);
        LinearLayout about=column();about.addView(text("Your review schedule",16,INK,true));gap(about,7);about.addView(text("New topics start with a review after 3 days. After each review, the next interval is 5, 7, 15 and 30 days. The next date is calculated from the actual day you mark the review complete, not the old planned date.",13,MUTED,false));gap(about,10);about.addView(text("After the 30-day review, the topic moves to Completed.",13,MUTED,false));card(content,about);
        LinearLayout danger=column();danger.addView(text("Delete all data",16,RED,true));gap(danger,6);danger.addView(text("Permanently deletes every subject, topic and review record stored by this app on this phone.",13,MUTED,false));gap(danger,12);TextView del=button("Delete all data",Color.rgb(255,235,235),RED);danger.addView(del,lp(-1,44));del.setOnClickListener(v->confirmDelete());card(content,danger);
        content.addView(text("Review Cycle  ·  Version 1.0",11,MUTED,false));
    }
    private String formatBytes(long b){if(b<1024)return b+" bytes";if(b<1024*1024)return String.format(Locale.getDefault(),"%.1f KB",b/1024.0);return String.format(Locale.getDefault(),"%.2f MB",b/(1024.0*1024));}
    private void confirmDelete(){new AlertDialog.Builder(this).setTitle("Delete all app data?").setMessage("This cannot be undone. All subjects, topics, review dates and completion history will be permanently erased from this device.").setNegativeButton("Cancel",null).setPositiveButton("Delete everything",(d,w)->{db.deleteAll();screen="home";render();Toast.makeText(this,"All review data deleted.",Toast.LENGTH_SHORT).show();}).show();}

    private static class Subject{long id;String name;Subject(long i,String n){id=i;name=n;}}
    private static class Topic{long id,subjectId,nextReview,completedAt;String title,subject;int stage;boolean overdue,dueToday;}
    private class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"review_cycle.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE subjects(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");d.execSQL("CREATE TABLE topics(id INTEGER PRIMARY KEY AUTOINCREMENT,subject_id INTEGER NOT NULL,title TEXT NOT NULL,stage INTEGER NOT NULL DEFAULT 0,next_review INTEGER NOT NULL,completed INTEGER NOT NULL DEFAULT 0,completed_at INTEGER NOT NULL DEFAULT 0,FOREIGN KEY(subject_id) REFERENCES subjects(id))");d.execSQL("CREATE TABLE review_history(id INTEGER PRIMARY KEY AUTOINCREMENT,topic_id INTEGER NOT NULL,stage INTEGER NOT NULL,reviewed_at INTEGER NOT NULL)");}
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}
        void addSubject(String n){try{getWritableDatabase().execSQL("INSERT INTO subjects(name) VALUES(?)",new Object[]{n});}catch(Exception e){Toast.makeText(MainActivity.this,"That subject already exists.",Toast.LENGTH_SHORT).show();}}
        void addTopic(long sid,String n,long created,long next){getWritableDatabase().execSQL("INSERT INTO topics(subject_id,title,stage,next_review) VALUES(?,?,0,?)",new Object[]{sid,n,next});}
        List<Subject> getSubjects(){List<Subject>a=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT id,name FROM subjects ORDER BY name COLLATE NOCASE",null);while(c.moveToNext())a.add(new Subject(c.getLong(0),c.getString(1)));c.close();return a;}
        List<Topic> getTopics(String where,String order){List<Topic>a=new ArrayList<>();String q="SELECT t.id,t.subject_id,t.title,t.stage,t.next_review,t.completed_at,s.name FROM topics t JOIN subjects s ON s.id=t.subject_id WHERE "+where+" ORDER BY "+order;Cursor c=getReadableDatabase().rawQuery(q,null);long day=today();while(c.moveToNext()){Topic t=new Topic();t.id=c.getLong(0);t.subjectId=c.getLong(1);t.title=c.getString(2);t.stage=c.getInt(3);t.nextReview=c.getLong(4);t.completedAt=c.getLong(5);t.subject=c.getString(6);t.overdue=t.nextReview<day;t.dueToday=t.nextReview==day;a.add(t);}c.close();return a;}
        void advanceTopic(long id,int stage,long next,long reviewed){SQLiteDatabase d=getWritableDatabase();d.execSQL("UPDATE topics SET stage=?,next_review=? WHERE id=?",new Object[]{stage,next,id});d.execSQL("INSERT INTO review_history(topic_id,stage,reviewed_at) VALUES(?,?,?)",new Object[]{id,stage,reviewed});}
        void completeTopic(long id,long when){SQLiteDatabase d=getWritableDatabase();d.execSQL("UPDATE topics SET completed=1,completed_at=? WHERE id=?",new Object[]{when,id});d.execSQL("INSERT INTO review_history(topic_id,stage,reviewed_at) VALUES(?,5,?)",new Object[]{id,when});}
        long storageBytes(){long n=0;String[] files={"review_cycle.db","review_cycle.db-wal","review_cycle.db-shm"};for(String f:files){File x=getDatabasePath(f);if(x.exists())n+=x.length();}return n;}
        void deleteAll(){SQLiteDatabase d=getWritableDatabase();d.execSQL("DELETE FROM review_history");d.execSQL("DELETE FROM topics");d.execSQL("DELETE FROM subjects");d.execSQL("DELETE FROM sqlite_sequence WHERE name IN ('subjects','topics','review_history')");d.execSQL("PRAGMA wal_checkpoint(FULL)");}
    }
}
