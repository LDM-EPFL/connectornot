/*
 * Copyright (C) 2009 Cyril Jaquier
 *
 * This file is part of NetCounter.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.lmag.connectornot.activity;

import net.lmag.connectornot.R;
import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;


/**
 * @author louis
 *
 */
public class NetCounterIndoorActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.indoor);
		
		LinearLayout wrap = new LinearLayout(this);
		wrap.setOrientation(1);
		
		LinearLayout line1 = new LinearLayout(this);
		Button btn;
		for(int i = 0; i < 5; i++) {
			final int temp = i;
			btn = new Button(this);
			btn.setText(i);
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					PointF location = new PointF(temp, (float) 0.0);
					
				}
			});
			line1.addView(btn);
		}
		
		LinearLayout line2 = new LinearLayout(this);
		for(int i = 5; i < 10; i++) {
			final int temp = i;
			btn = new Button(this);
			btn.setText(i);
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					PointF location = new PointF(temp, (float) 0.0);
					
				}
			});
			line2.addView(btn);
		}
		
		wrap.addView(line1);
		wrap.addView(line2);
		
		setContentView(wrap);
	}
	
	
}
