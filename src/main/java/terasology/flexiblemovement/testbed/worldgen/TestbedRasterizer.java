package terasology.flexiblemovement.testbed.worldgen;

import com.google.common.collect.Maps;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.Map;
import java.util.function.BiConsumer;

import static org.terasology.world.chunks.ChunkConstants.CHUNK_REGION;
import static org.terasology.world.chunks.ChunkConstants.CHUNK_SIZE;

public class TestbedRasterizer implements WorldRasterizer {
    public static final int MAX_CHUNK_DISTANCE = 3;
    public static final Vector3i STAIRS_CHUNK = new Vector3i(1, 0, 1);
    public static final Vector3i CHECKERBOARD_CHUNK = new Vector3i(-1, 0, 1);
    public static final Vector3i MAZE_CHUNK_1 = new Vector3i(-1, 0, -1);
    public static final Vector3i MAZE_CHUNK_2 = new Vector3i(-1, 0, -2);
    public static final Vector3i MAZE_CHUNK_3 = new Vector3i(-2, 0, -1);
    public static final Vector3i MAZE_CHUNK_4 = new Vector3i(-2, 0, -2);

    public static final int WORLD_FLOOR_HEIGHT = 0;
    private static final int CHUNK_FLOOR_HEIGHT = 0;

    private static Block sand;
    private static Block water;
    private static Block air;

    private BlockManager manager;
    private Map<Vector3i, BiConsumer<CoreChunk, Region>> terrainGenerators = Maps.newHashMap();

    @Override
    public void initialize() {
        manager = CoreRegistry.get(BlockManager.class);
        sand = manager.getBlock("CoreAssets:Sand");
        water = manager.getBlock("CoreAssets:Water");
        air = manager.getBlock("engine:air");

        // note that features are limited to CHUNK_SIZE which is 32x64x32
        terrainGenerators.put(STAIRS_CHUNK, TestbedRasterizer::createStairs);
        terrainGenerators.put(CHECKERBOARD_CHUNK, TestbedRasterizer::createCheckerboard);
        terrainGenerators.put(MAZE_CHUNK_1, TestbedRasterizer::createMaze);
        terrainGenerators.put(MAZE_CHUNK_2, TestbedRasterizer::createMaze);
        terrainGenerators.put(MAZE_CHUNK_3, TestbedRasterizer::createMaze);
        terrainGenerators.put(MAZE_CHUNK_4, TestbedRasterizer::createMaze);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        Vector3i chunkPos = chunk.getPosition();
        if (chunkPos.y != 0 || Math.abs(chunkPos.x) > MAX_CHUNK_DISTANCE || Math.abs(chunkPos.z) > MAX_CHUNK_DISTANCE) {
            return;
        }
        
        if (terrainGenerators.containsKey(chunkPos)) {
            terrainGenerators.get(chunkPos).accept(chunk, chunkRegion);
        }

        // otherwise just a flat surface
        Vector3i floorMin = new Vector3i(CHUNK_REGION.minX(), CHUNK_FLOOR_HEIGHT, CHUNK_REGION.minZ());
        Vector3i floorMax = new Vector3i(CHUNK_REGION.maxX(), CHUNK_FLOOR_HEIGHT, CHUNK_REGION.maxZ());
        for (Vector3i pos : Region3i.createBounded(floorMin, floorMax)) {
            chunk.setBlock(pos, sand);
        }
    }

    private static void createStairs(CoreChunk chunk, Region chunkRegion) {
        Vector3i start = CHUNK_REGION.min();
        Vector3i end = new Vector3i(CHUNK_REGION.maxX(), CHUNK_REGION.minY() + CHUNK_SIZE.y - 1, CHUNK_REGION.maxZ());

        for (Vector3i pos : Region3i.createBounded(start, end)) {
            double horizontalDistance = new Vector3i(start.x, 0, start.z).distance(new Vector3i(pos.x, 0, pos.z));
            if (pos.y - start.y < horizontalDistance + 1) {
                chunk.setBlock(pos, sand);
            } else {
                chunk.setBlock(pos, air);
            }
        }
    }

    private static void createCheckerboard(CoreChunk chunk, Region chunkRegion) {
        Vector3i start = CHUNK_REGION.min();
        Vector3i end = new Vector3i(CHUNK_REGION.maxX(), CHUNK_REGION.minY() + 2, CHUNK_REGION.maxZ());

        for (Vector3i pos : Region3i.createBounded(start, end)) {
            if ((pos.x + pos.z) % 2 == 0 || pos.y == start.y) {
                chunk.setBlock(pos, sand);
            } else {
                chunk.setBlock(pos, air);
            }
        }
    }

    private static void createMaze(CoreChunk chunk, Region chunkRegion) {
        Vector3i start = CHUNK_REGION.min();
        Vector3i end = new Vector3i(CHUNK_REGION.maxX(), CHUNK_REGION.minY() + 2, CHUNK_REGION.maxZ());

        for (Vector3i pos : Region3i.createBounded(start, end)) {
            if ((pos.x % 4 == 1 && pos.z % 4 != 0) || (pos.x % 4 == 3 && (pos.z + 2) % 4 != 0) || pos.y == start.y) {
                chunk.setBlock(pos, sand);
            } else {
                chunk.setBlock(pos, air);
            }
        }
    }
}
