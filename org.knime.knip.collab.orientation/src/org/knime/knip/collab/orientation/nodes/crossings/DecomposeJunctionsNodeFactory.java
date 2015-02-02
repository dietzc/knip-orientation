package org.knime.knip.collab.orientation.nodes.crossings;

import java.util.List;

import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.core.util.ImgUtils;

public class DecomposeJunctionsNodeFactory<L extends Comparable<L>> extends
		ValueToCellNodeFactory<ImgPlusValue<BitType>> {

	@Override
	public ValueToCellNodeModel<ImgPlusValue<BitType>, ImgPlusCell<BitType>> createNodeModel() {
		return new ValueToCellNodeModel<ImgPlusValue<BitType>, ImgPlusCell<BitType>>() {

			private ImgPlusCellFactory m_fac;

			@Override
			protected void prepareExecute(ExecutionContext exec) {
				super.prepareExecute(exec);
				m_fac = new ImgPlusCellFactory(exec);
			}

			@Override
			protected ImgPlusCell<BitType> compute(
					ImgPlusValue<BitType> cellValue) throws Exception {

				if (cellValue.getDimensions().length != 2) {
					throw new IllegalArgumentException(
							"Only two dimensions are supported since now");
				}

				ImgPlus<BitType> imgPlus = cellValue.getImgPlus();
				ImgPlus<BitType> res = new ImgPlus<BitType>(
						ImgUtils.createEmptyCopy(imgPlus), imgPlus);

				RectangleShape shape = new RectangleShape(1, true);
				IterableInterval<Neighborhood<BitType>> neighborhoods = shape.neighborhoods(Views.interval(
								Views.extendValue(imgPlus, new BitType(false)),
								imgPlus));

				RandomAccess<BitType> resAccess = res.randomAccess();
				RandomAccess<BitType> srcAccess = imgPlus.randomAccess();

				Cursor<Neighborhood<BitType>> neighborhoodCursor = neighborhoods
						.cursor();
				
				while (neighborhoodCursor.hasNext()) {
					neighborhoodCursor.fwd();

					srcAccess.setPosition(neighborhoodCursor);

					if (!srcAccess.get().get())
						continue;

					Cursor<BitType> oneNeighborhood = neighborhoodCursor.get()
							.cursor();

					int ctr = 0;
					while (oneNeighborhood.hasNext()) {
						oneNeighborhood.fwd();

						if (oneNeighborhood.get().get()) {
							ctr++;
						}
					}

					if (ctr != 0 && ctr <= 2) {
						resAccess.setPosition(neighborhoodCursor);
						resAccess.get().set(true);
					}
				}

				return m_fac.createCell(res);
			}

			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				//
			}
		};
	}

	@Override
	protected ValueToCellNodeDialog<ImgPlusValue<BitType>> createNodeDialog() {
		return new ValueToCellNodeDialog<ImgPlusValue<BitType>>() {

			@Override
			public void addDialogComponents() {
				// Nothing to do here since now ... add dialog component if
				// needed
			}
		};
	}
}
