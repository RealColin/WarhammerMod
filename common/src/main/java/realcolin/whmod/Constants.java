package realcolin.whmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	public static final String MOD_ID = "whmod";
	public static final String MOD_NAME = "WarhammerMod";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static final String MAP_BIOME_SOURCE_ID = "map_biome_source";
    public static final int CELL_SIZE = 512;
    public static final int CELL_BUFFER = 110; // might change back to 100
    public static final double BLEND_RANGE = 50;
}