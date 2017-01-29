/*
 * Universal Media Server, for streaming any media to DLNA
 * compatible renderers based on the http://www.ps3mediaserver.org.
 * Copyright (C) 2012 UMS developers.
 *
 * This program is a free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.dlna;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.drew.metadata.MetadataException;
import net.pms.image.ColorSpaceType;
import net.pms.image.GIFInfo;
import net.pms.image.ImageFormat;
import net.pms.image.ImageInfo;
import net.pms.image.JPEGInfo;
import net.pms.image.JPEGInfo.CompressionType;
import net.pms.image.JPEGSubsamplingNotation;
import net.pms.image.PNGInfo;
import net.pms.image.ExifInfo.ExifColorSpace;
import net.pms.image.PNGInfo.InterlaceMethod;


/**
 * Definition and validation of the different DLNA media profiles for images.
 * If more are added, corresponding changes need to be made in
 * {@link ImageFormat#fromImageProfile}.
 *
 * Please note: Only the profile constant (e.g {@code JPEG_TN} is taken into
 * consideration in {@link #equals(Object)} and {@link #hashCode()}, metadata
 * like {@code H} and {@code V} aren't compared. This means that:
 * <p>
 * {@code DLNAImageProfile.JPEG_RES_H_V.equals(DLNAImageProfile.JPEG_RES_H_V)}
 * <p>
 * is true even if {@code H} and {@code V} are different in the two profiles.
 */
public class DLNAImageProfile implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DLNAImageProfile.class);
	public static final int GIF_LRG_INT = 1;
	public static final int JPEG_LRG_INT = 2;
	public static final int JPEG_MED_INT = 3;
	public static final int JPEG_RES_H_V_INT = 4;
	public static final int JPEG_SM_INT = 5;
	public static final int JPEG_TN_INT = 6;
	public static final int PNG_LRG_INT = 7;
	public static final int PNG_TN_INT = 8;

	public static final Integer GIF_LRG_INTEGER = GIF_LRG_INT;
	public static final Integer JPEG_LRG_INTEGER = JPEG_LRG_INT;
	public static final Integer JPEG_MED_INTEGER = JPEG_MED_INT;
	public static final Integer JPEG_RES_H_V_INTEGER = JPEG_RES_H_V_INT;
	public static final Integer JPEG_SM_INTEGER = JPEG_SM_INT;
	public static final Integer JPEG_TN_INTEGER = JPEG_TN_INT;
	public static final Integer PNG_LRG_INTEGER = PNG_LRG_INT;
	public static final Integer PNG_TN_INTEGER = PNG_TN_INT;

	/**
	 * {@code GIF_LRG} maximum resolution 1600 x 1200.
	 */
	public static final DLNAImageProfile GIF_LRG = new DLNAImageProfile(GIF_LRG_INT, "GIF_LRG", 1600, 1200);

	/**
	 * {@code JPEG_LRG} maximum resolution 4096 x 4096.
	 */
	public static final DLNAImageProfile JPEG_LRG = new DLNAImageProfile(JPEG_LRG_INT, "JPEG_LRG", 4096, 4096);

	/**
	 * {@code JPEG_MED} maximum resolution 1024 x 768.
	 */
	public static final DLNAImageProfile JPEG_MED = new DLNAImageProfile(JPEG_MED_INT, "JPEG_MED", 1024, 768);

	/**
	 * {@code JPEG_RES_H_V} exact resolution H x V.<br>
	 * <br>
	 * <b>This constant creates an invalid profile, only for use with
	 * {@link #equals(Object)}.</b><br>
	 * <br>
	 * To create a valid profile, use {@link #createJPEG_RES_H_V(int, int)} instead.
	 */
	public static final DLNAImageProfile JPEG_RES_H_V = new DLNAImageProfile(JPEG_RES_H_V_INT, "JPEG_RES_H_V", -1, -1);

	/**
	 * {@code JPEG_SM} maximum resolution 640 x 480.
	 */
	public static final DLNAImageProfile JPEG_SM = new DLNAImageProfile(JPEG_SM_INT, "JPEG_SM", 640, 480);

	/**
	 * {@code JPEG_TN} maximum resolution 160 x 160.
	 */
	public static final DLNAImageProfile JPEG_TN = new DLNAImageProfile(JPEG_TN_INT, "JPEG_TN", 160, 160);

	/**
	 * {@code PNG_LRG} maximum resolution 4096 x 4096.
	 */
	public static final DLNAImageProfile PNG_LRG = new DLNAImageProfile(PNG_LRG_INT, "PNG_LRG", 4096, 4096);

	/**
	 * {@code PNG_TN} maximum resolution 160 x 160.
	 */
	public static final DLNAImageProfile PNG_TN = new DLNAImageProfile(PNG_TN_INT, "PNG_TN", 160, 160);

	/**
	 * Creates a new {@link #JPEG_RES_H_V} profile instance with the exact
	 * resolution H x V. Set {@code H} and {@code V} for this instance. Not
	 * applicable for other profiles.
	 *
	 * @param horizontal the {@code H} value.
	 * @param vertical the {@code V} value.
	 * @return The new {@link #JPEG_RES_H_V} instance.
	 */
	public static DLNAImageProfile createJPEG_RES_H_V(int horizontal, int vertical) {
		return new DLNAImageProfile(
			JPEG_RES_H_V_INT,
			"JPEG_RES_" + Integer.toString(horizontal) + "_" + Integer.toString(vertical),
			horizontal,
			vertical
		);
	}

	public final int imageProfileInt;
	public final String imageProfileStr;
	private final int horizontal;
	private final int vertical;

	/**
	 * Instantiate a {@link DLNAImageProfile} object.
	 */
	private DLNAImageProfile(int imageProfileInt, String imageProfileStr, int horizontal, int vertical) {
		this.imageProfileInt = imageProfileInt;
		this.imageProfileStr = imageProfileStr;
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	/**
	 * Returns the string representation of this {@link DLNAImageProfile}.
	 */
	@Override
	public String toString() {
		return imageProfileStr;
	}

	/**
	 * Returns the integer representation of this {@link DLNAImageProfile}.
	 */
	public int toInt() {
		return imageProfileInt;
	}

	/**
	 * Converts a {@link DLNAImageProfile} to an {@link Integer} object.
	 *
	 * @return This {@link DLNAImageProfile}'s {@link Integer} mapping.
	 */
	public Integer toInteger() {
		switch (imageProfileInt) {
			case GIF_LRG_INT:
				return GIF_LRG_INTEGER;
			case JPEG_LRG_INT:
				return JPEG_LRG_INTEGER;
			case JPEG_MED_INT:
				return JPEG_MED_INTEGER;
			case JPEG_RES_H_V_INT:
				return JPEG_RES_H_V_INTEGER;
			case JPEG_SM_INT:
				return JPEG_SM_INTEGER;
			case JPEG_TN_INT:
				return JPEG_TN_INTEGER;
			case PNG_LRG_INT:
				return PNG_LRG_INTEGER;
			case PNG_TN_INT:
				return PNG_TN_INTEGER;
			default:
				throw new IllegalStateException("DLNAImageProfile " + imageProfileStr + ", " + imageProfileInt + " is unknown.");
		}
	}

	/**
	 * Converts the {@link String} passed as argument to a
	 * {@link DLNAImageProfile}. If the conversion fails, this method returns
	 * {@code null}.
	 */
	public static DLNAImageProfile toDLNAImageProfile(String argument) {
		return toDLNAImageProfile(argument, null);
	}

	/**
	 * Converts the integer passed as argument to a {@link DLNAImageProfile}.
	 * If the conversion fails, this method returns {@code null}.
	 */
	public static DLNAImageProfile toDLNAImageProfile(int value) {
		return toDLNAImageProfile(value, null);
	}

	/**
	 * Converts the integer passed as argument to a {@link DLNAImageProfile}. If the
	 * conversion fails, this method returns the specified default.
	 */
	public static DLNAImageProfile toDLNAImageProfile(int value, DLNAImageProfile defaultImageProfile) {
		switch (value) {
			case GIF_LRG_INT:
				return GIF_LRG;
			case JPEG_LRG_INT:
				return JPEG_LRG;
			case JPEG_MED_INT:
				return JPEG_MED;
			case JPEG_RES_H_V_INT:
				return JPEG_RES_H_V;
			case JPEG_SM_INT:
				return JPEG_SM;
			case JPEG_TN_INT:
				return JPEG_TN;
			case PNG_LRG_INT:
				return PNG_LRG;
			case PNG_TN_INT:
				return PNG_TN;
			default:
				return defaultImageProfile;
		}
	}

	/**
	 * Converts the {@link String} passed as argument to a
	 * {@link DLNAImageProfile}. If the conversion fails, this method returns
	 * the specified default.
	 */
	public static DLNAImageProfile toDLNAImageProfile(String argument, DLNAImageProfile defaultImageProfile) {
		if (argument == null) {
			return defaultImageProfile;
		}

		argument = argument.toUpperCase(Locale.ROOT);
		switch (argument) {
			case "GIF_LRG":
				return DLNAImageProfile.GIF_LRG;
			case "JPEG_LRG":
				return DLNAImageProfile.JPEG_LRG;
			case "JPEG_MED":
				return DLNAImageProfile.JPEG_MED;
			case "JPEG_RES_H_V":
				return DLNAImageProfile.JPEG_RES_H_V;
			case "JPEG_SM":
				return DLNAImageProfile.JPEG_SM;
			case "JPEG_TN":
				return DLNAImageProfile.JPEG_TN;
			case "PNG_LRG":
				return DLNAImageProfile.PNG_LRG;
			case "PNG_TN":
				return DLNAImageProfile.PNG_TN;
			default:
				if (argument.startsWith("JPEG_RES")) {
					Matcher matcher = Pattern.compile("^JPEG_RES_?(\\d+)[X_](\\d+)").matcher(argument);
					if (matcher.find()) {
						return new DLNAImageProfile(
							JPEG_RES_H_V_INT,
							"JPEG_RES_" + matcher.group(1) + "_" + matcher.group(2),
							Integer.parseInt(matcher.group(1)),
							Integer.parseInt(matcher.group(2))
						);
					}
				}
				return defaultImageProfile;
		}
	}

	/**
	 * @return The {@link ImageFormat} for this {@link DLNAImageProfile}.
	 */
	public ImageFormat getFormat() {
		switch (imageProfileInt) {
			case GIF_LRG_INT:
				return ImageFormat.GIF;
			case JPEG_LRG_INT:
			case JPEG_MED_INT:
			case JPEG_RES_H_V_INT:
			case JPEG_SM_INT:
			case JPEG_TN_INT:
				return ImageFormat.JPEG;
			case PNG_LRG_INT:
			case PNG_TN_INT:
				return ImageFormat.PNG;
			default:
				throw new IllegalStateException("Profile is missing from switch statement");
		}
	}

	/**
	 * @return The mime type for this {@link DLNAImageProfile}.
	 */
	public String getMimeType() {
		switch (getFormat()) {
			case GIF:
				return "image/gif";
			case JPEG:
				return "image/jpeg";
			case PNG:
				return "image/png";
			default:
				throw new IllegalStateException("Format is missing from switch statement");
		}
	}

	/**
	 * Gets the maximum image width for the given {@link DLNAImageProfile}.
	 *
	 * @return The value in pixels.
	 */
	public int getMaxWidth() {
		return horizontal;
	}

	/**
	 * Get {@code H} for the {@code JPEG_RES_H_V} profile.
	 *
	 * @return The {@code H} value in pixels.
	 */
	public int getH() {
		return horizontal;
	}

	/**
	 * Gets the maximum image height for the given {@link DLNAImageProfile}.
	 *
	 * @return The value in pixels.
	 */
	public int getMaxHeight() {
		return vertical;
	}

	/**
	 * Get {@code V} for the {@code JPEG_RES_H_V} profile.
	 *
	 * @return The {@code V} value in pixels.
	 */
	public int getV() {
		return vertical;
	}

	/**
	 * Get the {@link Dimension} of the maximum image resolution for the given
	 * {@link DLNAImageProfile}.
	 *
	 * @return The {@link Dimension}.
	 */
	public Dimension getMaxResolution() {
		if (horizontal >= 0 && vertical >= 0) {
			return new Dimension(horizontal, vertical);
		}
		return new Dimension();
	}

	/**
	 * Evaluates whether the thumbnail or the image itself should be used as
	 * the source for the given profile when two sources are available. This is
	 * typically only relevant for image resources.
	 *
	 * @param imageInfo the {@link ImageInfo} for the non-thumbnail source.
	 * @param thumbnailImageInfo the {@link ImageInfo} for the thumbnail source.
	 * @return The evaluation result.
	 */
	public boolean useThumbnailSource(ImageInfo imageInfo, ImageInfo thumbnailImageInfo) {
		if (DLNAImageProfile.JPEG_TN.equals(this) || DLNAImageProfile.PNG_TN.equals(this)) {
			// These should always use thumbnail as the source
			return true;
		}

		if (
			imageInfo == null || imageInfo.getWidth() < 1 || imageInfo.getHeight() < 1 ||
			thumbnailImageInfo == null || thumbnailImageInfo.getWidth() < 1 || thumbnailImageInfo.getHeight() < 1
		) {
			// Impossible to do a valid evaluation under these circumstances
			return false;
		}

		boolean thumbIsSmaller =
			thumbnailImageInfo.getWidth() < imageInfo.getWidth() ||
			thumbnailImageInfo.getHeight() < imageInfo.getHeight();
		if (!thumbIsSmaller) {
			// Thumbnail has as good a resolution as the source, we might as
			// well use the thumbnail if the format matches
			return getFormat() == thumbnailImageInfo.getFormat() || getFormat() != imageInfo.getFormat();
		}

		// At this point we know that the thumbnail is smaller than the source.
		// Only use the thumbnail if it's bigger or equal in size to this profile.
		return
			getMaxWidth() <= thumbnailImageInfo.getWidth() &&
			getMaxHeight() <= thumbnailImageInfo.getHeight();
	}

	/**
	 * Performs GIF compliance checks. The result is stored in
	 * {@code complianceResult}.
	 */
	protected static void checkGIF(
		ImageInfo imageInfo,
		InternalComplianceResult complianceResult
	) throws MetadataException {

		if (imageInfo == null || imageInfo.getFormat() != ImageFormat.GIF || !(imageInfo instanceof GIFInfo)) {
			return;
		}
		complianceResult.colorsCorrect = true;
		GIFInfo gifInfo = (GIFInfo) imageInfo;
		complianceResult.formatCorrect = "89a".equals(gifInfo.getFormatVersion());
		if (!complianceResult.formatCorrect) {
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"GIF DLNA compliance failed with wrong GIF version \"%s\"",
					gifInfo.getFormatVersion()
				));
			} else {
				complianceResult.failures.add("GIF version");
			}
		}
	}

	/**
	 * Performs JPEG compliance checks. The result is stored in
	 * {@code complianceResult}.
	 */
	protected static void checkJPEG(
		ImageInfo imageInfo,
		InternalComplianceResult complianceResult
	) throws MetadataException {

		if (imageInfo == null || imageInfo.getFormat() != ImageFormat.JPEG || !(imageInfo instanceof JPEGInfo)) {
			return;
		}
		JPEGInfo jpegInfo = (JPEGInfo) imageInfo;
		complianceResult.colorsCorrect =
			jpegInfo.getColorSpaceType() == ColorSpaceType.TYPE_GRAY ||
			jpegInfo.getColorSpaceType() == ColorSpaceType.TYPE_RGB ||
			jpegInfo.getColorSpaceType() == ColorSpaceType.TYPE_YCbCr;
		if (!complianceResult.colorsCorrect) {
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"JPEG DLNA compliance failed with illegal color space \"%s\"",
					jpegInfo.getColorSpaceType()
				));
			} else {
				complianceResult.failures.add("color space");
			}
		}

		if (
			jpegInfo.getExifColorSpace() != null &&
			jpegInfo.getExifColorSpace() != ExifColorSpace.SRGB &&
			jpegInfo.getExifColorSpace() != ExifColorSpace.UNCALIBRATED
		) {
			complianceResult.colorsCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"JPEG DLNA compliance failed with illegal Exif color space \"%s\"",
					jpegInfo.getExifColorSpace()
				));
			} else {
				complianceResult.failures.add("Exif color space");
			}
		}

		complianceResult.formatCorrect = jpegInfo.getJFIFVersion() >= 0x102 || jpegInfo.getExifVersion() >= 100;
		if (!complianceResult.formatCorrect && LOGGER.isTraceEnabled()) {
			String jfifVersion = jpegInfo.getJFIFVersion() == ImageInfo.UNKNOWN ? null :
				Double.toHexString((double) jpegInfo.getJFIFVersion() / 0x100)
			;
			if (jfifVersion != null) {
				jfifVersion = jfifVersion.replaceFirst("0x", "").replaceFirst("p-?\\d*", "");
			}
			String exifVersion = jpegInfo.getExifVersion() == ImageInfo.UNKNOWN ? null :
				Double.toString((double) jpegInfo.getExifVersion() / 100);

			if (jfifVersion == null && exifVersion == null) {
				if (LOGGER.isTraceEnabled()) {
					complianceResult.failures.add("JPEG DLNA compliance failed with missing Exif- and JFIF version");
				} else {
					complianceResult.failures.add("missing version information");
				}
			} else if (jfifVersion == null) {
				if (LOGGER.isTraceEnabled()) {
					complianceResult.failures.add(String.format(
						"JPEG DLNA compliance failed with too low Exif version \"%s\"",
						exifVersion
					));
				} else {
					complianceResult.failures.add("Exif version");
				}
			} else if (exifVersion == null) {
				if (LOGGER.isTraceEnabled()) {
					complianceResult.failures.add(String.format(
						"JPEG DLNA compliance failed with too low JFIF version \"%s\"",
						jfifVersion
					));
				} else {
					complianceResult.failures.add("JFIF version");
				}
			} else {
				if (LOGGER.isTraceEnabled()) {
					complianceResult.failures.add(String.format(
						"JPEG DLNA compliance failed with too low Exif version \"%s\" and JFIF version \"%s\"",
						exifVersion,
						jfifVersion
					));
				} else {
					complianceResult.failures.add("Exif and JFIF version");
				}
			}
		}
		if (jpegInfo.getCompressionType() != CompressionType.BASELINE_HUFFMAN) {
			complianceResult.formatCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"JPEG DLNA compliance failed with illegal compression type \"%s\"",
					jpegInfo.getCompressionType()
				));
			} else {
				complianceResult.failures.add("compression type");
			}
		}
		if (jpegInfo.getBitDepth() != 8) {
			complianceResult.formatCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"JPEG DLNA compliance failed with illegal bit depth \"%d\"",
					jpegInfo.getBitDepth()
				));
			} else {
				complianceResult.failures.add("bit depth");
			}
		}
		if (jpegInfo.getNumComponents() != 3 && jpegInfo.getNumComponents() != 1) {
			complianceResult.formatCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"JPEG DLNA compliance failed with illegal number of components \"%d\"",
					jpegInfo.getNumComponents()
				));
			} else {
				complianceResult.failures.add("number of components");
			}
		}
		if (!jpegInfo.isTypicalHuffman()) {
			complianceResult.formatCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add("JPEG DLNA compliance failed with non-typical Huffman tables");
			} else {
				complianceResult.failures.add("optimized");
			}
		}
		if (
			jpegInfo.getNumComponents() == 3 &&
			!(new JPEGSubsamplingNotation(4, 2, 2)).equals(jpegInfo.getChromaSubsampling()) &&
			!(new JPEGSubsamplingNotation(4, 2, 0)).equals(jpegInfo.getChromaSubsampling())
		) {
			complianceResult.formatCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"JPEG DLNA compliance failed with illegal chroma subsampling \"%s\"",
					jpegInfo.getChromaSubsampling()
				));
			} else {
				complianceResult.failures.add("chroma subsampling");
			}
		}
	}

	/**
	 * Performs PNG compliance checks. The result is stored in
	 * {@code complianceResult}.
	 */
	protected static void checkPNG(
		ImageInfo imageInfo,
		InternalComplianceResult complianceResult
	) throws MetadataException {

		if (imageInfo == null || imageInfo.getFormat() != ImageFormat.PNG || !(imageInfo instanceof PNGInfo)) {
			return;
		}

		PNGInfo pngInfo = (PNGInfo) imageInfo;
		if (pngInfo.getColorType() != null && pngInfo.getBitDepth() == 8) {
			switch (pngInfo.getColorType()) {
				case Greyscale:
				case GreyscaleWithAlpha:
				case IndexedColor:
				case TrueColor:
				case TrueColorWithAlpha:
					complianceResult.colorsCorrect = true;
					break;
				default:
					if (LOGGER.isTraceEnabled()) {
						complianceResult.failures.add(String.format(
							"PNG DLNA compliance failed with illegal color type \"%s\"",
							pngInfo.getColorType()
						));
					} else {
						complianceResult.failures.add("color type");
					}
			}
		} else if (LOGGER.isTraceEnabled()) {
			if (pngInfo.getColorType() == null) {
				if (LOGGER.isTraceEnabled()) {
					complianceResult.failures.add("PNG DLNA compliance failed with missing color type information");
				} else {
					complianceResult.failures.add("missing color type");
				}
			} else {
				if (LOGGER.isTraceEnabled()) {
					complianceResult.failures.add(String.format(
						"PNG DLNA compliance failed with illegal bit depth \"%d\"",
						pngInfo.getBitDepth()
					));
				} else {
					complianceResult.failures.add("bit depth");
				}
			}
		}
		if (pngInfo.isModifiedBitDepth()) {
			complianceResult.colorsCorrect = false;
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add("PNG DLNA compliance failed with non-standard bit depth");
			} else {
				complianceResult.failures.add("non-standard bit depth");
			}
		}

		complianceResult.formatCorrect = pngInfo.getInterlaceMethod() == InterlaceMethod.NONE;
		if (!complianceResult.formatCorrect && LOGGER.isTraceEnabled()) {
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"PNG DLNA compliance failed with illegal interlace method \"%s\"",
					pngInfo.getInterlaceMethod()
				));
			} else {
				complianceResult.failures.add("interlace method");
			}
		}
	}

	public static DLNAImageProfile getClosestDLNAProfile(
		int width,
		int height,
		ImageFormat format,
		boolean allowJPEG_RES_H_V
	) {
		if (format == null) {
			throw new NullPointerException("format cannot be null");
		}
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException(String.format(
				"Cannot find DLNA media format profile for %d x %d",
				width,
				height
			));
		}

		switch (format) {
			case GIF:
				return GIF_LRG;
			case JPEG:
				if (allowJPEG_RES_H_V) {
					return createJPEG_RES_H_V(width, height);
				}
				if (JPEG_TN.isResolutionCorrect(width, height)) {
					return JPEG_TN;
				}
				if (JPEG_SM.isResolutionCorrect(width, height)) {
					return JPEG_SM;
				}
				if (JPEG_MED.isResolutionCorrect(width, height)) {
					return JPEG_MED;
				}
				return JPEG_LRG;
			case PNG:
				if (PNG_TN.isResolutionCorrect(width, height)) {
					return PNG_TN;
				}
				return PNG_LRG;
			default:
				throw new IllegalArgumentException(
					"Invalid format " + format + " for which to find a DLNA media format profile"
				);
		}
	}

	/**
	 * Validates DLNA compliance for {@code imageInfo} against the largest
	 * {@link DLNAImageProfile} for the given {@link ImageFormat}. Validation
	 * is performed on resolution, format, format version, format compression
	 * and color coding. The validation result is returned in a
	 * {@link DLNAComplianceResult} data structure.
	 *
	 * @param imageInfo the {@link ImageInfo} for the image to check for DLNA
	 *                  compliance.
	 * @param format the {@link ImageFormat} to check for DLNA compliance
	 *               against.
	 * @return The validation result.
	 */
	public static DLNAComplianceResult checkCompliance(ImageInfo imageInfo, ImageFormat format) {
		if (imageInfo == null) {
			throw new NullPointerException("imageInfo cannot be null");
		}
		if (format == null) {
			throw new NullPointerException("format cannot be null");
		}

		InternalComplianceResult complianceResult = new InternalComplianceResult();
		try {
			DLNAImageProfile largestProfile;
			switch (format) {
				case JPEG:
					// Since JPEG_RES_H_V has no upper limit, any resolution is ok
					complianceResult.maxWidth = Integer.MAX_VALUE;
					complianceResult.maxHeight = Integer.MAX_VALUE;
					complianceResult.resolutionCorrect = true;
					checkJPEG(imageInfo, complianceResult);
					break;
				case GIF:
					largestProfile = DLNAImageProfile.GIF_LRG;
					complianceResult.maxWidth = largestProfile.getMaxWidth();
					complianceResult.maxHeight = largestProfile.getMaxHeight();
					complianceResult.resolutionCorrect = largestProfile.isResolutionCorrect(imageInfo);
					if (!complianceResult.resolutionCorrect) {
						if (LOGGER.isTraceEnabled()) {
							complianceResult.failures.add(String.format(
								"GIF DLNA compliance failed with wrong resolution %d x %d (limits are %d x %d)",
								imageInfo.getWidth(),
								imageInfo.getHeight(),
								largestProfile.getMaxWidth(),
								largestProfile.getMaxHeight()
							));
						} else {
							complianceResult.failures.add("resolution");
						}
					}
					checkGIF(imageInfo, complianceResult);
					break;
				case PNG:
					largestProfile = DLNAImageProfile.PNG_LRG;
					complianceResult.maxWidth = largestProfile.getMaxWidth();
					complianceResult.maxHeight = largestProfile.getMaxHeight();
					complianceResult.resolutionCorrect = largestProfile.isResolutionCorrect(imageInfo);
					if (!complianceResult.resolutionCorrect) {
						if (LOGGER.isTraceEnabled()) {
							complianceResult.failures.add(String.format(
								"PNG DLNA compliance failed with wrong resolution %d x %d (limits are %d x %d)",
								imageInfo.getWidth(),
								imageInfo.getHeight(),
								largestProfile.getMaxWidth(),
								largestProfile.getMaxHeight()
							));
						} else {
							complianceResult.failures.add("resolution");
						}
					}
					checkPNG(imageInfo, complianceResult);
					break;
				default:
					if (LOGGER.isTraceEnabled()) {
						complianceResult.failures.add(String.format(
							"DLNA compliance failed with illegal image format \"%s\"",
							format
						));
					} else {
						complianceResult.failures.add("illegal format");
					}
			}
		} catch (MetadataException e) {
			complianceResult.formatCorrect = false;
			complianceResult.colorsCorrect = false;
			LOGGER.debug("MetadataException in checkCompliance: {}", e.getMessage());
			LOGGER.trace("", e);
		}

		return DLNAComplianceResult.toDLNAComplianceResult(complianceResult);
	}

	/**
	 * Validates DLNA compliance for {@code imageInfo} against a specific
	 * {@link DLNAImageProfile}. Validation is performed on resolution, format,
	 * format version, format compression and color coding. The validation
	 * result is returned in a {@link DLNAComplianceResult} data structure.
	 *
	 * @param imageInfo the {@link ImageInfo} for the image to check for DLNA
	 *                  compliance.
	 * @param profile the {@link DLNAImageProfile} to check for DLNA compliance
	 *               against.
	 * @return The validation result.
	 */
	public static DLNAComplianceResult checkCompliance(ImageInfo imageInfo, DLNAImageProfile profile) {
		if (imageInfo == null || profile == null) {
			return DLNAComplianceResult.toDLNAComplianceResult(new InternalComplianceResult());
		}
		return profile.checkCompliance(imageInfo);
	}

	/**
	 * Determines whether the resolution specified in {@code imageInfo} is valid
	 * for this {@link DLNAImageProfile}.
	 *
	 * @param imageInfo the {@link ImageInfo} to evaluate.
	 * @return {@code true} if the resolution is valid, {@code false} otherwise.
	 */
	public boolean isResolutionCorrect(ImageInfo imageInfo) {
		if (imageInfo == null) {
			return false;
		}
		return isResolutionCorrect(imageInfo.getWidth(), imageInfo.getHeight());
	}

	/**
	 * Determines whether the resolution specified in {@code width} and
	 * {@code height} is valid for this {@link DLNAImageProfile}.
	 *
	 * @param width the number of horizontal pixels for this resolution.
	 * @param height the number of vertical pixels for this resolution.
	 * @return {@code true} if the resolution is valid, {@code false} otherwise.
	 */
	public boolean isResolutionCorrect(int width, int height) {
		if (width < 1 || height < 1) {
			return false;
		}
		if (equals(DLNAImageProfile.JPEG_RES_H_V)) {
			return
				width == horizontal &&
				height == vertical;
		} else {
			return
				width <= horizontal &&
				height <= vertical;
		}
	}

	/**
	 * Validates DLNA compliance for {@code image} against this
	 * {@link DLNAImageProfile}. Validation is performed on resolution, format,
	 * format version, format compression and color coding. The validation
	 * result is returned in a {@link DLNAComplianceResult} data structure.
	 *
	 * @param imageInfo the {@link ImageInfo} for the image to check for DLNA
	 *                  compliance.
	 * @return The validation result.
	 */
	public DLNAComplianceResult checkCompliance(ImageInfo imageInfo) {
		InternalComplianceResult complianceResult = new InternalComplianceResult();
		complianceResult.maxWidth = horizontal;
		complianceResult.maxHeight = vertical;
		if (imageInfo == null) {
			return DLNAComplianceResult.toDLNAComplianceResult(complianceResult);
		}
		complianceResult.resolutionCorrect = isResolutionCorrect(imageInfo);
		if (!complianceResult.resolutionCorrect) {
			if (LOGGER.isTraceEnabled()) {
				complianceResult.failures.add(String.format(
					"%s DLNA compliance failed with wrong resolution %d x %d (limits are %d x %d)",
					toString(),
					imageInfo.getWidth(),
					imageInfo.getHeight(),
					horizontal,
					vertical
				));
			} else {
				complianceResult.failures.add("resolution");
			}
		}

		try {
			switch (this.toInt()) {
				case DLNAImageProfile.GIF_LRG_INT:
					checkGIF(imageInfo, complianceResult);
					break;
				case DLNAImageProfile.JPEG_LRG_INT:
				case DLNAImageProfile.JPEG_MED_INT:
				case DLNAImageProfile.JPEG_RES_H_V_INT:
				case DLNAImageProfile.JPEG_SM_INT:
				case DLNAImageProfile.JPEG_TN_INT:
					checkJPEG(imageInfo, complianceResult);
					break;
				case DLNAImageProfile.PNG_LRG_INT:
				case DLNAImageProfile.PNG_TN_INT:
					checkPNG(imageInfo, complianceResult);
					break;
				default:
					throw new IllegalStateException("Illegal DLNA media profile");
			}
		} catch (MetadataException e) {
			complianceResult.formatCorrect = false;
			complianceResult.colorsCorrect = false;
			LOGGER.debug("MetadataException in checkCompliance: {}", e.getMessage());
			LOGGER.trace("", e);
		}

		return DLNAComplianceResult.toDLNAComplianceResult(complianceResult);
	}

	/**
	 * Calculates the would-be resolution and size of {@code imageInfo} if that
	 * image were to be converted into this profile. Size cannot be calculated
	 * if any actual conversion is needed (finding that would require an actual
	 * encoding to be done), and will return {@code null} unless the image can
	 * be used as-is.
	 *
	 * @param imageInfo the {@link ImageInfo} for the image in question.
	 *
	 * @return The result in a {@link HypotheticalResult} data structure.
	 */
	public HypotheticalResult calculateHypotheticalProperties(ImageInfo imageInfo) {
		if (imageInfo == null) {
			throw new IllegalArgumentException("calculateHypotheticalProperties: imageInfo cannot be null");
		}
		DLNAComplianceResult complianceResult = checkCompliance(imageInfo);
		if (complianceResult.isAllCorrect()) {
			return new HypotheticalResult(
				imageInfo.getWidth(),
				imageInfo.getHeight(),
				imageInfo.getSize(),
				false
			);
		}
		if (complianceResult.resolutionCorrect) {
			return new HypotheticalResult(imageInfo.getWidth(), imageInfo.getHeight(), null, true);
		}
		double widthScale = imageInfo.getWidth() < 1 ? 0 : (double) horizontal / imageInfo.getWidth();
		double heightScale = imageInfo.getHeight() < 1 ? 0 : (double) vertical / imageInfo.getHeight();
		// We never scale up because the DLNA profiles only have resolution
		// ceilings - limit scale to max 1.
		double scale = Math.min(Math.min(widthScale, heightScale), 1);
		return new HypotheticalResult(
			(int) Math.round(imageInfo.getWidth() * scale),
			(int) Math.round(imageInfo.getHeight() * scale),
			null,
			true
		);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + imageProfileInt;
		return result;
	}

	/**
	 * Only the profile constant is considered when comparing, metadata like
	 * {@code H} and {@code V} aren't taken into account.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DLNAImageProfile)) {
			return false;
		}
		DLNAImageProfile other = (DLNAImageProfile) obj;
		if (imageProfileInt != other.imageProfileInt) {
			return false;
		}
		return true;
	}

	/**
	 * Immutable {@link #calculateHypotheticalProperties(ImageInfo)}
	 * result data structure. Please note that {@code size} might be {@code null}.
	 */
	public static class HypotheticalResult {
		/**
		 * The calculated width or {@link ImageInfo#UNKNOWN} if unknown.
		 */
		public final int width;
		/**
		 * The calculated height or {@link ImageInfo#UNKNOWN} if unknown.
		 */
		public final int height;
		/**
		 * The size or {@code null} if unknown.
		 */
		public final Long size;
		/**
		 * Whether conversion is needed.
		 */
		public final boolean conversionNeeded;

		public HypotheticalResult(int width, int height, Long size, boolean conversionNeeded) {
			if (width < 1 || height < 1) {
				// Use ImageInfo.UNKNOWN for unknown
				this.width = ImageInfo.UNKNOWN;
				this.height = ImageInfo.UNKNOWN;
			} else {
				this.width = width;
				this.height = height;
			}
			this.size = size;
			this.conversionNeeded = conversionNeeded;
		}

		@Override
		public String toString() {
			return
				"HypotheticalResult [width=" + width + ", height=" + height +
				", size=" + size + ", conversionNeeded=" + conversionNeeded +
				"]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (conversionNeeded ? 1231 : 1237);
			result = prime * result + height;
			result = prime * result + ((size == null) ? 0 : size.hashCode());
			result = prime * result + width;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof HypotheticalResult)) {
				return false;
			}
			HypotheticalResult other = (HypotheticalResult) obj;
			if (conversionNeeded != other.conversionNeeded) {
				return false;
			}
			if (height != other.height) {
				return false;
			}
			if (size == null) {
				if (other.size != null) {
					return false;
				}
			} else if (!size.equals(other.size)) {
				return false;
			}
			if (width != other.width) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Internal mutable compliance result data structure.
	 */
	protected static class InternalComplianceResult {
		public boolean resolutionCorrect = false;
		public boolean formatCorrect = false;
		public boolean colorsCorrect = false;
		public int maxWidth;
		public int maxHeight;
		public List<String> failures = new ArrayList<>();
	}

	/**
	 * Immutable compliance result data structure.
	 */
	public static class DLNAComplianceResult {
		private final boolean resolutionCorrect;
		private final boolean formatCorrect;
		private final boolean colorsCorrect;
		private final boolean allCorrect;
		private final int maxWidth;
		private final int maxHeight;
		private final List<String> failures;

		public DLNAComplianceResult(
			boolean resolutionCorrect,
			boolean formatCorrect,
			boolean colorsCorrect,
			int maxWidth,
			int maxHeight,
			List<String> failures
		) {
			this.resolutionCorrect = resolutionCorrect;
			this.formatCorrect = formatCorrect;
			this.colorsCorrect = colorsCorrect;
			this.allCorrect = resolutionCorrect && formatCorrect && colorsCorrect;
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			this.failures = failures;
		}

		protected static DLNAComplianceResult toDLNAComplianceResult(InternalComplianceResult result) {
			return new DLNAComplianceResult(
				result.resolutionCorrect,
				result.formatCorrect,
				result.colorsCorrect,
				result.maxWidth,
				result.maxHeight,
				result.failures
			);
		}

		/**
		 * @return Whether the resolution is within DLNA limits for the given
		 *         profile.
		 */
		public boolean isResolutionCorrect() {
			return resolutionCorrect;
		}

		/**
		 * @return Whether the format/compression is compliant for the given
		 *         profile.
		 */
		public boolean isFormatCorrect() {
			return formatCorrect;
		}

		/**
		 * @return Whether the color coding is compliant for the given profile.
		 */
		public boolean isColorsCorrect() {
			return colorsCorrect;
		}

		/**
		 * @return Whether all tests show compliance.
		 */
		public boolean isAllCorrect() {
			return allCorrect;
		}

		/**
		 * @return The maximum width.
		 */
		public int getMaxWidth() {
			return maxWidth;
		}

		/**
		 * @return the maximum height.
		 */
		public int getMaxHeight() {
			return maxHeight;
		}

		/**
		 * @return The {@link List} of failure messages.
		 */
		public List<String> getFailures() {
			return Collections.unmodifiableList(this.failures);
		}
	}
}
