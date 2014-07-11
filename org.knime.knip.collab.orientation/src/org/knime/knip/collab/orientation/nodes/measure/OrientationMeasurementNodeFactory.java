package org.knime.knip.collab.orientation.nodes.measure;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataCell;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;

public class OrientationMeasurementNodeFactory<T extends RealType<T>> extends
		ValueToCellNodeFactory<ImgPlusValue<BitType>> {

	@Override
	protected ValueToCellNodeDialog<ImgPlusValue<BitType>> createNodeDialog() {
		return new OrientationMeasurementNodeDialog();
	}

	@Override
	public ValueToCellNodeModel<ImgPlusValue<BitType>, ? extends DataCell> createNodeModel() {
		return new OrientationMeasurementNodeModel();
	}

}
