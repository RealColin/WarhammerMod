package realcolin.whmod.worldgen.map;

import ij.plugin.filter.EDM;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import org.apache.batik.gvt.GraphicsNode;
import realcolin.whmod.Constants;
import realcolin.whmod.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;

public class Cell {

    private final BufferedImage regionMap;
    private final BufferedImage bufferedRegionMap;

    private final HashMap<Integer, double[][]> distanceTransforms;

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

        distanceTransforms = new HashMap<>();
        createDistTransforms(cellPos);

//        try {
//            String filename = String.format("cell_%d_%d.png",
//                    cellPos.a(),
//                    cellPos.b());
//            File output = new File(filename);
//            ImageIO.write(bufferedRegionMap, "png", output);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public int getColorAt(int x, int z) {
        //return regionMap.getRGB(Math.floorMod(x, Constants.CELL_SIZE), Math.floorMod(z, Constants.CELL_SIZE));
        return bufferedRegionMap.getRGB(Math.floorMod(x, Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2), Math.floorMod(z, Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2));
    }

    private void createDistTransforms(Pair cellPos) {
        // get every unique color in the buffered map
        var colors = new HashSet<Integer>();
        for (int x = 0; x < bufferedRegionMap.getWidth(); x++) {
            for (int y = 0; y < bufferedRegionMap.getHeight(); y++) {
                var color = bufferedRegionMap.getRGB(x, y);
                colors.add(color);
            }
        }

        for (var color : colors) {
            var mask = new ByteProcessor(bufferedRegionMap.getWidth(), bufferedRegionMap.getHeight());
            for (int x = 0; x < bufferedRegionMap.getWidth(); x++) {
                for (int y = 0; y < bufferedRegionMap.getHeight(); y++) {
                    mask.set(x, y, bufferedRegionMap.getRGB(x, y) == color ? 0 : 255);
                }
            }

            EDM edm = new EDM();
            FloatProcessor edt = edm.makeFloatEDM(mask, 0, false);

            int width = edt.getWidth();
            int height = edt.getHeight();
            double[][] dists = new double[width][height];

            double maxDist = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    dists[x][y] = edt.getf(x, y);
                    maxDist = Math.max(maxDist, dists[x][y]);
                }
            }

//            if (cellPos.a() == 98 && cellPos.b() == 59) {
//                System.out.println("=== Color " + Integer.toHexString(color) + " ===");
//                System.out.println("Corner (0,0): " + dists[0][0]);
//                System.out.println("Corner (" + (width-1) + ",0): " + dists[width-1][0]);
//                System.out.println("Corner (0," + (height-1) + "): " + dists[0][height-1]);
//                System.out.println("Corner (" + (width-1) + "," + (height-1) + "): " + dists[width-1][height-1]);
//                System.out.println("Center (" + (width/2) + "," + (height/2) + "): " + dists[width/2][height/2]);
//                System.out.println("Max distance: " + maxDist);
//                System.out.println();
//            }

//            BufferedImage debugImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    // Normalize distance to 0-255
//                    int brightness = (int) ((dists[x][y] / maxDist) * 255);
//                    int rgb = (brightness << 16) | (brightness << 8) | brightness;
//                    debugImg.setRGB(x, y, rgb);
//                }
//            }
//
//            try {
//                // Save with color in filename
//                String filename = String.format("edt_cell_%d_%d_color_%s.png",
//                        cellPos.a(),
//                        cellPos.b(),
//                        Integer.toHexString(color));
//                File output = new File(filename);
//                ImageIO.write(debugImg, "png", output);
////                System.out.println("Saved EDT visualization: " + output.getAbsolutePath());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            distanceTransforms.put(color, dists);
        }
    }

    public HashMap<Integer, double[][]> getDistTransforms() {
        return distanceTransforms;
    }

    public int getClosestColorWithinBlendRange(int x, int z) {
        int ox = Math.floorMod(x, Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2);
        int oz = Math.floorMod(z, Constants.CELL_SIZE) + (Constants.CELL_BUFFER / 2);

        var dist = Constants.BLEND_RANGE;
        var color = bufferedRegionMap.getRGB(ox, oz);

        for (var c : distanceTransforms.keySet()) {
            var arr = distanceTransforms.get(c);
            var val = arr[ox][oz];

            if (val < dist) {
                dist = val;
                color = c;
            }
        }

        return color;
    }
}
