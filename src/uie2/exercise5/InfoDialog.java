package uie2.exercise5;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class InfoDialog extends Dialog {
	int x;
	int y;
	String date;
	String data;
	View layout;

	public InfoDialog(Context context, int x, int y, String date, String data,
			View layout) {
		super(context);
		this.x = x;
		this.y = y;
		this.date = date;
		this.data = data;
		this.layout = layout;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infodialog);
		TextView textDate = (TextView) findViewById(R.id.infodialog_date);
		TextView textData = (TextView) findViewById(R.id.infodialog_data);
		textDate.setText(date);
		textData.setText(data);
		textDate.setOnClickListener(new android.view.View.OnClickListener() {
			
			public void onClick(View v) {
				finishThis();
			}
		});
		textData.setOnClickListener(new android.view.View.OnClickListener() {
			
			public void onClick(View v) {
				finishThis();
			}
		});
		;
		x = x - (int) layout.getWidth() / 2;
		y = y - (int) layout.getHeight() / 2;
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.x = x;
		params.y = y;
		this.getWindow().setAttributes(params);
	}

	private void finishThis() {
		this.dismiss();
	}
}
