package org.knime.knip.collab.orientation.nodes.measure;

import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.exceptions.KNIPException;
import org.knime.knip.base.node.ValueToCellNodeModel;

public class OrientationMeasurementNodeModel extends
		ValueToCellNodeModel<ImgPlusValue<BitType>, DoubleCell> {

	// variable m_minPixels (User defined in GUI):
	// minimum size of the picture to be able to calculate an (representative)
	// angle
	public static final double DEFAULT_REFANGLE = 0.0;
	public static final int DEFAULT_MINPIXELS = 5;

	private final SettingsModelIntegerBounded m_minPixels = createSettingsModelminPixels();
	private final SettingsModelDoubleBounded m_refAngle = createSettingsModelrefAngle();

	static SettingsModelIntegerBounded createSettingsModelminPixels() {
		return new SettingsModelIntegerBounded("min_Pixels",
				OrientationMeasurementNodeModel.DEFAULT_MINPIXELS, 3,
				Integer.MAX_VALUE);
	}

	static SettingsModelDoubleBounded createSettingsModelrefAngle() {
		return new SettingsModelDoubleBounded("ref_angle",
				OrientationMeasurementNodeModel.DEFAULT_REFANGLE, -180, 180);
	}

	@Override
	protected DoubleCell compute(ImgPlusValue<BitType> cellValue)
			throws Exception {

		// input image.
		Img<BitType> img = cellValue.getImgPlus();

		// filter illegal images (wrong type, or too many dimensions).
		if (!(img.firstElement() instanceof BitType)) {
			throw new KNIPException(
					"Input image not of type BitType. Image Will be ignored");
		}
		if (img.numDimensions() > 2) {
			throw new KNIPException(
					"Can only compute with two dimensional images. Image Will be ignored");
		}

		// filter too small images.
		if (img.size() < m_minPixels.getIntValue()) {
			throw new KNIPException("Input image is too small (must have min "
					+ m_minPixels.getIntValue() + " pixels). Filtered.");
		}

		// the (later calculated) angle.
		double angle = 0;

		RectangleShape shape = new RectangleShape(1, true);

		IterableInterval<Neighborhood<BitType>> neighborhoods = shape // all
																// neighborhoods
																// of the image
				.neighborhoods(Views.interval( // including:
						Views.extendValue(img, new BitType(false)), img)); // extend
																			// border
																			// of
																			// img
																			// with
																			// false
																			// BitTypes
																			// (=
																			// 0
																			// values)

		// cursor over each pixel of the image.
		Cursor<BitType> c = img.cursor();

		// used to determinate if the first or second start/end point was found.
		boolean first = true;

		// the start and end point to calculate the vector
		int[] one = new int[c.numDimensions()];
		int[] two = new int[c.numDimensions()];

		// vectors to calculate the angle
		int[] vector = new int[c.numDimensions()];
		final int[] yAxis = { 0, 1 };

		// neighborhoods and Cursor c are in sync.
		for (Neighborhood<BitType> neighborhood : neighborhoods) {

			// if the pixel is not white it will be ignored.
			// also increments the image cursor.
			if (!c.next().get()) {
				continue;
			}

			// iterator over the neighbors of the current pixel.
			Cursor<BitType> nc = neighborhood.cursor();

			// number of white neighbors.
			byte count = 0;

			// look for white neighbors (if two are found it can't be a
			// start/end point).
			while (nc.hasNext()) {
				if (nc.next().get()) {
					count++;
					if (count == 2) {
						break;
					}
				}
			}

			// if the pixel is not start or end point continue with next pixel.
			if (count == 2) {
				continue;
			}

			// if for some reason a white pixel has no white neighbors
			if (count == 0) {
				throw new KNIPException("Sole Pixel! Wrong image input!");
			}

			// point was found: Is this the first or second point? (start / end
			// point)
			if (first) {
				first = false;
				c.localize(one);
			} else {
				c.localize(two);

				// make sure one <= two (in the X-dimension), to calculate a
				// distinct vector (and angle).
				if (one[0] > two[0]) {
					int[] temp = one;
					one = two;
					two = temp;
				}

				// both points found.
				break;
			}
		}

		for (int i = 0; i < one.length; i++) {
			// vector[0] will always be 0 or bigger, since two[0] >= one[0] (see
			// if () above)
			vector[i] = two[i] - one[i];
		}

		// calculate the angle (in degrees).
		angle = m_refAngle.getDoubleValue()
				+ 180
				- Math.toDegrees(Math.acos(scalarMult(vector, yAxis)
						/ vectorLength(vector)));

		if (Double.isNaN(angle))
			throw new KNIPException("Circle! Wrong image input!");

		return new DoubleCell(angle);
	}

	// calculate the scalar product of two vectors. (warning: arrays should have
	// the same length!)
	private int scalarMult(int[] a, int[] b) {
		int res = 0;
		for (int i = 0; i < a.length; i++) { // if a.length != b.length: i <
												// Math.min(a.length, b.length)
			res += (a[i] * b[i]);
		}
		return res;
	}

	// calculate the length of a vector.
	private double vectorLength(int[] a) {
		return Math.sqrt(scalarMult(a, a));
	}

	@Override
	protected void addSettingsModels(List<SettingsModel> settingsModels) {
		settingsModels.add(m_minPixels);
		settingsModels.add(m_refAngle);
	}

};
