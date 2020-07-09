package terasology.flexiblemovement.testbed.worldgen;

import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

@RegisterWorldGenerator(id = "TestbedGenerator", displayName = "Testbed Generator")
public class TestbedGenerator extends BaseFacetedWorldGenerator {
    
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    
    public TestbedGenerator(SimpleUri uri) {
        super(uri);
    }
    
    @Override
    protected WorldBuilder createWorld() {
        return new WorldBuilder(worldGeneratorPluginLibrary).setSeaLevel(0).addRasterizer(new TestbedRasterizer());
    }
    
    @Override
    public Vector3f getSpawnPosition(EntityRef entity) {
        return new Vector3f(0, TestbedRasterizer.WORLD_FLOOR_HEIGHT + 1, 0);
    }
}
