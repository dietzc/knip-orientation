package org.knime.knip.collab.orientation.nodes.measure;

import net.imglib2.type.logic.BitType;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;

public class OrientationMeasurementNodeDialog extends
		ValueToCellNodeDialog<ImgPlusValue<BitType>> {

	// TODO get the variable minPixels to work/show.
	@Override
	public void addDialogComponents() {
		// minimum pixels of an image
		addDialogComponent(new DialogComponentNumber(
				OrientationMeasurementNodeModel.createSettingsModelminPixels(),
				"Minimum Size", /* step */1));
		
		addDialogComponent(new DialogComponentNumber(
				OrientationMeasurementNodeModel.createSettingsModelrefAngle(),
				"RefAngle Size", /* step */1));
	}
}
