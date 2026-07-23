package com.fumbbl.ffb.client.layout;

import com.fumbbl.ffb.client.ClientLayout;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Describes the major regions of a client layout.
 *
 * This class owns layout topology: side rails, pitch area, and panel area.
 * It does not place individual Swing components inside those regions.
 */

class LayoutAreas {

	private static final int LOG_CHAT_GAP = 2;
	private static final int PANEL_BORDER = 1;

	enum InfoPosition {
		BOTTOM, RIGHT
	}

	final Rectangle homeRail;
	final Rectangle awayRail;
	final Rectangle pitchArea;
	final Rectangle infoArea;
	final InfoPosition infoPosition;

	private LayoutAreas(Rectangle homeRail, Rectangle awayRail, Rectangle pitchArea, Rectangle infoArea,
		InfoPosition infoPosition) {
		this.homeRail = homeRail;
		this.awayRail = awayRail;
		this.pitchArea = pitchArea;
		this.infoArea = infoArea;
		this.infoPosition = infoPosition;
	}

	static LayoutAreas arrange(ClientLayout layout, Rectangle content, int railWidth, Dimension score, Dimension log,
		Dimension chat) {
		switch (layout) {
			case PORTRAIT:
				return portrait(content, railWidth, bottomInfoSize(score, log, chat));
			case SQUARE:
				return square(content, railWidth, rightInfoSize(score, log, chat));
			default:
				return landscape(content, railWidth, bottomInfoSize(score, log, chat));
		}
	}

	static Dimension naturalSize(ClientLayout layout, Dimension rail, Dimension pitch, Dimension score, Dimension log,
		Dimension chat) {
		switch (layout) {
			case PORTRAIT:
				return portraitNaturalSize(rail, pitch, bottomInfoSize(score, log, chat));
			case SQUARE:
				return squareNaturalSize(rail, pitch, rightInfoSize(score, log, chat));
			default:
				return landscapeNaturalSize(rail, pitch, bottomInfoSize(score, log, chat));
		}
	}

	private static LayoutAreas landscape(Rectangle content, int railWidth, Dimension infoSize) {
		Rectangle homeRail = leftStrip(content, railWidth);
		Rectangle awayRail = rightStrip(content, railWidth);
		Rectangle centerColumn = betweenHorizontal(homeRail, awayRail, content);
		Rectangle infoArea = bottomStrip(centerColumn, infoSize.height);
		Rectangle pitchArea = above(centerColumn, infoArea);

		return new LayoutAreas(
			homeRail,
			awayRail,
			pitchArea,
			infoArea,
			InfoPosition.BOTTOM
		);
	}

	private static LayoutAreas portrait(Rectangle content, int railWidth, Dimension infoSize) {
		Rectangle infoArea = bottomStrip(content, infoSize.height);
		Rectangle gameArea = above(content, infoArea);
		Rectangle homeRail = leftStrip(gameArea, railWidth);
		Rectangle awayRail = rightStrip(gameArea, railWidth);
		Rectangle pitchArea = betweenHorizontal(homeRail, awayRail, gameArea);

		return new LayoutAreas(
			homeRail,
			awayRail,
			pitchArea,
			infoArea,
			InfoPosition.BOTTOM
		);
	}

	private static LayoutAreas square(Rectangle content, int railWidth, Dimension infoSize) {
		Rectangle infoArea = rightStrip(content, infoSize.width);
		Rectangle gameArea = leftOf(content, infoArea);
		Rectangle homeRail = leftStrip(gameArea, railWidth);
		Rectangle awayRail = rightStrip(gameArea, railWidth);
		Rectangle pitchArea = betweenHorizontal(homeRail, awayRail, gameArea);

		return new LayoutAreas(
			homeRail,
			awayRail,
			pitchArea,
			infoArea,
			InfoPosition.RIGHT
		);
	}

	private static Rectangle leftStrip(Rectangle rectangle, int width) {
		return new Rectangle(rectangle.x, rectangle.y, width, rectangle.height);
	}

	private static Rectangle rightStrip(Rectangle rectangle, int width) {
		return new Rectangle(rectangle.x + rectangle.width - width, rectangle.y, width, rectangle.height);
	}

	private static Rectangle bottomStrip(Rectangle rectangle, int height) {
		int availableHeight = Math.max(1, rectangle.height - height);
		return new Rectangle(rectangle.x, rectangle.y + availableHeight, rectangle.width, height);
	}

	private static Rectangle leftOf(Rectangle rectangle, Rectangle rightStrip) {
		return new Rectangle(rectangle.x, rectangle.y, Math.max(1, rightStrip.x - rectangle.x), rectangle.height);
	}

	private static Rectangle above(Rectangle rectangle, Rectangle bottomStrip) {
		return new Rectangle(rectangle.x, rectangle.y, rectangle.width, Math.max(1, bottomStrip.y - rectangle.y));
	}

	private static Rectangle betweenHorizontal(Rectangle left, Rectangle right, Rectangle bounds) {
		return new Rectangle(left.x + left.width, bounds.y, Math.max(1, right.x - (left.x + left.width)), bounds.height);
	}

	private static Dimension landscapeNaturalSize(Dimension rail, Dimension pitch, Dimension infoSize) {
		int centerWidth = Math.max(pitch.width, infoSize.width);
		int centerHeight = pitch.height + infoSize.height;
		return new Dimension(rail.width + centerWidth + rail.width, Math.max(rail.height, centerHeight));
	}

	private static Dimension portraitNaturalSize(Dimension rail, Dimension pitch, Dimension infoSize) {
		int gameWidth = rail.width + pitch.width + rail.width;
		int gameHeight = Math.max(rail.height, pitch.height);
		return new Dimension(Math.max(gameWidth, infoSize.width), gameHeight + infoSize.height);
	}

	private static Dimension squareNaturalSize(Dimension rail, Dimension pitch, Dimension infoSize) {
		int gameWidth = rail.width + pitch.width + rail.width;
		int gameHeight = Math.max(rail.height, pitch.height);
		return new Dimension(gameWidth + infoSize.width, Math.max(gameHeight, infoSize.height));
	}

	Rectangle finalInfoArea(Rectangle pitchBounds) {
		if (infoPosition == InfoPosition.BOTTOM) {
			return new Rectangle(infoArea.x, pitchBounds.y + pitchBounds.height, infoArea.width, infoArea.height);
		}
		return new Rectangle(infoArea);
	}

	private static Dimension bottomInfoSize(Dimension score, Dimension log, Dimension chat) {
		Dimension logChat = logChatSize(log, chat);
		return new Dimension(Math.max(score.width, logChat.width), score.height + logChat.height);
	}

	private static Dimension rightInfoSize(Dimension score, Dimension log, Dimension chat) {
		return new Dimension(Math.max(log.width, Math.max(score.width, chat.width)) + (2 * PANEL_BORDER),
			log.height + score.height + chat.height + (2 * PANEL_BORDER));
	}

	private static Dimension logChatSize(Dimension log, Dimension chat) {
		return new Dimension(log.width + LOG_CHAT_GAP + chat.width + (2 * PANEL_BORDER),
			Math.max(log.height, chat.height) + (2 * PANEL_BORDER));
	}
}
