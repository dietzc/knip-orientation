package org.knime.knip.base.nodes.proc.hough;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.base.node.dialog.DialogComponentDimSelection;
import org.knime.knip.base.node.nodesettings.SettingsModelDimSelection;
import org.knime.knip.core.ops.transform.HoughLine;
import org.knime.knip.core.types.ImgFactoryTypes;
import org.knime.knip.core.types.NativeTypes;
import org.knime.knip.core.util.EnumUtils;

/**
 * @author dietzc
 */
public final class HoughLineNodeFactory<T extends RealType<T> & NativeType<T>, S extends RealType<S> & NativeType<S>>
		extends ValueToCellNodeFactory<ImgPlusValue<T>> {

	private static SettingsModelDimSelection createDimSelectionModel() {
		return new SettingsModelDimSelection("dim_selection", "X", "Y");
	}

	private static SettingsModelIntegerBounded createRhoBinsModel() {
		return new SettingsModelIntegerBounded("rhobins", 0, 0,
				Integer.MAX_VALUE);
	}

	private static SettingsModelIntegerBounded createThetaBinsModel() {
		return new SettingsModelIntegerBounded("thetabins", 0, 0,
				Integer.MAX_VALUE);
	}

	private static SettingsModelIntegerBounded createThresholdModel() {
		return new SettingsModelIntegerBounded("threshold", 0,
				-Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	private static SettingsModelString createOutTypeModel() {
		return new SettingsModelString("outtype",
				NativeTypes.BYTETYPE.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueToCellNodeModel<ImgPlusValue<T>, ImgPlusCell<S>> createNodeModel() {
		return new ValueToCellNodeModel<ImgPlusValue<T>, ImgPlusCell<S>>() {

			private SettingsModelDimSelection m_dimSelection = createDimSelectionModel();

			private SettingsModelIntegerBounded m_rhoBins = createRhoBinsModel();

			private SettingsModelIntegerBounded m_thetaBins = createThetaBinsModel();

			private SettingsModelIntegerBounded m_threshold = createThresholdModel();

			private SettingsModelString m_outType = createOutTypeModel();

			private ImgPlusCellFactory m_imgCellFactory;

			@Override
			protected void addSettingsModels(
					final List<SettingsModel> settingsModels) {
				settingsModels.add(m_dimSelection);
				settingsModels.add(m_rhoBins);
				settingsModels.add(m_thetaBins);
				settingsModels.add(m_threshold);
				settingsModels.add(m_outType);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void prepareExecute(final ExecutionContext exec) {
				m_imgCellFactory = new ImgPlusCellFactory(exec);
			}

			@Override
			protected ImgPlusCell<S> compute(final ImgPlusValue<T> cellValue)
					throws Exception {

				final ImgPlus<T> img = cellValue.getImgPlusCopy();

				// TODO more elegant?
				@SuppressWarnings("unchecked")
				final S outType = (S) NativeTypes.getTypeInstance(NativeTypes
						.valueOf(m_outType.getStringValue()));

				final T threshold = img.firstElement().createVariable();
				threshold.setReal(m_threshold.getIntValue());

				final HoughLine<T, S, Img<T>> houghOp = new HoughLine<T, S, Img<T>>(
						outType, threshold, m_rhoBins.getIntValue(),
						m_thetaBins.getIntValue());

				final Img<S> res = SubsetOperations.iterate(houghOp,
						m_dimSelection.getSelectedDimIndices(img), img, houghOp
								.bufferFactory().instantiate(img));

				return m_imgCellFactory.createCell(res, img);
			}

		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ValueToCellNodeDialog<ImgPlusValue<T>> createNodeDialog() {
		return new ValueToCellNodeDialog<ImgPlusValue<T>>() {

			@Override
			public void addDialogComponents() {

				addDialogComponent("Options", "",
						new DialogComponentDimSelection(
								createDimSelectionModel(),
								"Dimension selection", 2, 2));

				addDialogComponent(
						"Options",
						"Factory Selection",
						new DialogComponentStringSelection(
								new SettingsModelString("factoryselection",
										ImgFactoryTypes.SOURCE_FACTORY
												.toString()),
								"Factory Type",
								EnumUtils
										.getStringListFromToString(ImgFactoryTypes
												.values())));

				addDialogComponent("Options", "Parameters",
						new DialogComponentNumber(createRhoBinsModel(),
								"Number of Rho bins", 1));

				addDialogComponent("Options", "Parameters",
						new DialogComponentNumber(createThetaBinsModel(),
								"Number of Theta bins", 1));

				addDialogComponent(
						"Options",
						"Parameters",
						new DialogComponentNumber(
								createThresholdModel(),
								"Threshold value (pixels > threshold are regarded as active)",
								1));

				addDialogComponent(
						"Options",
						"Target Type",
						new DialogComponentStringSelection(
								new SettingsModelString("outtype",
										NativeTypes.BYTETYPE.toString()),
								"Target format", EnumUtils
										.getStringListFromToString(NativeTypes
												.values())));
			}
		};
	}
}
