package realcolin.whmod.worldgen.map;

import com.mojang.datafixers.kinds.Const;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import realcolin.whmod.Constants;
import realcolin.whmod.WHRegistries;
import realcolin.whmod.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorldMap {

    public static final Codec<WorldMap> DIRECT_CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    ResourceLocation.CODEC.fieldOf("image").forGetter(src -> src.imageLoc),
                    Codec.INT.fieldOf("resolution").forGetter(src -> src.resolution),
                    Biome.CODEC.fieldOf("default_biome").forGetter(src -> src.defaultBiome),
                    Terrain.CODEC.fieldOf("default_terrain").forGetter(src -> src.defaultTerrain),
                    MapEntry.ENTRIES_CODEC.fieldOf("entries").forGetter(src -> src.entries)
            ).apply(inst, WorldMap::new));

    public static final Codec<Holder<WorldMap>> CODEC = RegistryFileCodec.create(WHRegistries.MAP, DIRECT_CODEC);

    private final ResourceLocation imageLoc;
    private final int resolution; // blocks per inch
    private final Holder<Biome> defaultBiome;
    private final Holder<Terrain> defaultTerrain;
    private final List<MapEntry> entries;

    private final Set<Holder<Terrain>> terrains;
    private final HashMap<Integer, MapEntry> colorRegionMap;
    private final int width;
    private final int height;
    private final GraphicsNode node;
    private final ConcurrentHashMap<Pair, BufferedImage> cache;

    private final ConcurrentHashMap<Pair, Cell> cellCache;

    public WorldMap(ResourceLocation imageLoc, int resolution, Holder<Biome> defaultBiome, Holder<Terrain> defaultTerrain, List<MapEntry> entries) {
        this.imageLoc = imageLoc;
        this.resolution = resolution;
        this.defaultBiome = defaultBiome;
        this.defaultTerrain = defaultTerrain;
        this.entries = entries;
        this.cache = new ConcurrentHashMap<>();
        this.cellCache = new ConcurrentHashMap<>();

        String PATH = "assets/%s/map/%s".formatted(imageLoc.getNamespace(), imageLoc.getPath());

        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            InputStream svgFile = WorldMap.class.getResourceAsStream("/" + PATH);
            SVGDocument svgDocument = factory.createSVGDocument(null, svgFile);

            GVTBuilder builder = new GVTBuilder();
            BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
            node = builder.build(ctx, svgDocument);

            this.width = Math.round((svgDocument.getRootElement().getWidth().getBaseVal().getValue() / 96) * resolution);
            this.height = Math.round((svgDocument.getRootElement().getHeight().getBaseVal().getValue() / 96) * resolution);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        terrains = new HashSet<>();
        colorRegionMap = new HashMap<>();
        for (var e : entries) {
            terrains.add(e.terrain());

            var c = new Color(e.color());
            colorRegionMap.put(c.getRGB(), e);
        }
        terrains.add(defaultTerrain);

        Constants.LOG.info("Successfully Initialized a WorldMap instance");
    }

    public Holder<Biome> getDefaultBiome() {
        return defaultBiome;
    }

    public Set<Holder<Biome>> getAllBiomes() {
        var set = new HashSet<Holder<Biome>>();
        set.add(defaultBiome);

        for (var e : entries) {
            set.addAll(e.biomes().stream().map(MapEntry.BiomePair::biome).toList());
        }

        return set;
    }

    public Holder<Biome> getBiome(int x, int z) {
        var color = getColorAtPixel(x, z);

        if (color != -1 && colorRegionMap.containsKey(color))
            return colorRegionMap.get(color).biomes().getFirst().biome(); // TODO change this to select a biome with noise somehow

        return defaultBiome;
    }

    public Set<Holder<Terrain>> getTerrains() {
        return terrains;
    }

    public Cell getCellAt(int x, int z) {
        var cellPos = new Pair(Math.floorDiv(x, Constants.CELL_SIZE), Math.floorDiv(z, Constants.CELL_SIZE));

        if (cellCache.containsKey(cellPos))
            return cellCache.get(cellPos);
        else {
            var cell = new Cell(this.node, this.resolution, cellPos);
            cellCache.put(cellPos, cell);
            return cell;
        }
    }

    public Terrain getTerrainFromColor(int color) {
        if (color != -1 && colorRegionMap.containsKey(color)) {
            return colorRegionMap.get(color).terrain().value();
        }

        return defaultTerrain.value();
    }

    public Terrain getTerrain(int x, int z) {
        var color = getColorAtPixel(x, z);

        if (color != -1 && colorRegionMap.containsKey(color))
            return colorRegionMap.get(color).terrain().value();

        return defaultTerrain.value();
    }

    public Terrain getClosestNeighbor(int x, int z) {
        var color = getColorAtPixel(x, z);
        var cellPos = new Pair(Math.floorDiv(x, Constants.CELL_SIZE), Math.floorDiv(z, Constants.CELL_SIZE));
        var cell = cellCache.get(cellPos);

        var nearest = cell.getClosestColorWithinBlendRange(x, z);

        if (nearest == color)
            return null;
        else
            return colorRegionMap.get(nearest).terrain().value();
    }


    private int getColorAtPixel(int x, int y) {
        if (outsideRange(x, y))
            return -1;

        var cellPos = new Pair(Math.floorDiv(x, Constants.CELL_SIZE), Math.floorDiv(y, Constants.CELL_SIZE));
        Cell cell;

        if (cellCache.containsKey(cellPos))
            cell = cellCache.get(cellPos);
        else {
            var start = System.nanoTime();
            cell = new Cell(this.node, this.resolution, cellPos);
            var elapsed = System.nanoTime() - start;
//            System.out.println("Cell generated in " + elapsed + " nanoseconds.");

            cellCache.put(cellPos, cell);
        }

        // uses world-coordinates for this because translation takes place within - maybe change this
        return cell.getColorAt(x, y);

//        int cellSize = 512;
//        var cellPos = new Pair(x / cellSize, y / cellSize);
//        BufferedImage regionMap;
//
//        if (cache.containsKey(cellPos)) {
//            regionMap = cache.get(cellPos);
//        } else {
//            regionMap = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_RGB);
//            var g2d = regionMap.createGraphics();
//
//            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//            int svgX = cellPos.a() * cellSize;
//            int svgY = cellPos.b() * cellSize;
//            g2d.translate(-svgX, -svgY);
//            g2d.scale((double) resolution / 96.0, (double)resolution / 96.0);
//            this.node.paint(g2d);
//
//            cache.put(cellPos, regionMap);
//        }
//
//        return regionMap.getRGB(x % cellSize, y % cellSize);
    }

    private boolean outsideRange(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }
}
