package realcolin.whmod.worldgen.map;

import org.apache.batik.gvt.GraphicsNode;
import realcolin.whmod.Constants;
import realcolin.whmod.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Cell {

    private BufferedImage regionMap;
    private BufferedImage bufferedRegionMap;

    private HashMap<Integer, BufferedImage> distanceTransforms;

    public Cell(GraphicsNode node, int resolution, Pair cellPos) {
        regionMap = new BufferedImage(Constants.CELL_SIZE, Constants.CELL_SIZE, BufferedImage.TYPE_INT_RGB);
        var g2d = regionMap.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        int svgX = cellPos.a() * Constants.CELL_SIZE;
        int svgY = cellPos.b() * Constants.CELL_SIZE;
        g2d.translate(-svgX, -svgY);
        g2d.scale((double) resolution / 96.0, (double)resolution / 96.0);
        node.paint(g2d);

        bufferedRegionMap = new BufferedImage(Constants.CELL_SIZE + Constants.CELL_BUFFER, Constants.CELL_SIZE + Constants.CELL_BUFFER, BufferedImage.TYPE_INT_RGB);
        var g2dd = bufferedRegionMap.createGraphics();

        g2dd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2dd.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        svgX = cellPos.a() * Constants.CELL_SIZE - (Constants.CELL_BUFFER / 2);
        svgY = cellPos.b() * Constants.CELL_SIZE - (Constants.CELL_BUFFER / 2);

        g2dd.translate(-svgX, -svgY);
        g2dd.scale((double) resolution / 96.0, (double) resolution / 96.0);
        node.paint(g2dd);
    }

    public int getColorAt(int x, int z) {
        return regionMap.getRGB(Math.floorMod(x, Constants.CELL_SIZE), Math.floorMod(z, Constants.CELL_SIZE));
    }
}
