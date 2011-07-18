package uie2.exercise5;

import android.content.Context;

public class Application extends android.app.Application {

	private static Application app;

	@Override
	public void onCreate() {
		super.onCreate();
		Application.app = this;
	}
	
	public static Context getContext() {
		return app;
	}
}
