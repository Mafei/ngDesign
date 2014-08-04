/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.internal.views.graph;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import com.synflow.models.dpn.Direction;

/**
 * This class defines a port anchor.
 * 
 * @author Matthieu Wipliez
 *
 */
public class PortAnchor extends AbstractConnectionAnchor {

	private Direction direction;

	public PortAnchor(IFigure owner, Direction direction) {
		super(owner);
		this.direction = direction;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PortAnchor) {
			PortAnchor other = (PortAnchor) obj;
			return other.direction == this.direction && other.getOwner() == this.getOwner();
		}
		return false;
	}

	@Override
	public Point getLocation(Point reference) {
		Rectangle bounds = getOwner().getBounds();
		Point location = bounds.getLocation();
		getOwner().translateToAbsolute(location);

		if (direction == Direction.INCOMING) {
			location.translate(0, bounds.height / 2);
		} else {
			location.translate(bounds.width, bounds.height / 2);
		}
		return location;
	}

	@Override
	public Point getReferencePoint() {
		Point ref = getOwner().getBounds().getCenter();
		getOwner().translateToAbsolute(ref);
		return ref;
	}

	@Override
	public int hashCode() {
		return direction.hashCode() ^ getOwner().hashCode();
	}

}
