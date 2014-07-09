/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ui.internal.views;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class WaveSignal extends Canvas {

	private int[] values;

	private Color white;

	public WaveSignal(Composite parent, int style) {
		super(parent, style);

		addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
			
		});
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				onDispose(e);
			}
		});

		white = new Color(null, 255, 255, 255);
		setBackground(white);
	}
	
	public void addValues(int... values) {
		this.values = values;
	}

	private void onDispose(DisposeEvent event) {
		white.dispose();
	}

	private void onPaint(PaintEvent event) {
		if (values == null || values.length == 0) {
			return;
		}

		GC gc = event.gc;
		int lastX = 0, lastY = 20;
		for (int i = 0; i < values.length; i++) {
			int newY = values[i] != 0 ? 0 : 20;
			if (lastY != newY) {
				gc.drawLine(lastX, lastY, lastX, newY);
				lastY = newY;
			}
			gc.drawLine(lastX, lastY, lastX + 20, lastY);
			lastX += 20;
		}
	}

}
