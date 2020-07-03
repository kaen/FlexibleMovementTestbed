/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package terasology.flexiblemovement.testbed;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.flexiblemovement.FlexibleMovementComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

/**
 * Sets up the landscape and spawns some characters for testing
 */
@Share(TestbedSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class TestbedSystem extends BaseComponentSystem {
    private static final String MODULE_NAME = "flexiblemovementtestbed";
    private static final int SURFACE_HEIGHT = 41;

    @In private WorldProvider worldProvider;
    @In private BlockManager blockManager;
    @In private PrefabManager prefabManager;
    @In private EntityManager entityManager;
    @In private InventoryManager inventoryManager;

    private Block air;
    private Block dirt;
    private Block ocean;

    @Override
    public void postBegin() {
        super.postBegin();
        loadBlockTypes();
        prepareTestbed();
        spawnTestCharacters();
    }

    @ReceiveEvent(components = InventoryComponent.class)
    public void onPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        inventoryManager.giveItem(player, player, entityManager.create("flexiblemovementtestbed:setTarget"));

        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        EntityRef planks = blockFactory.newInstance(blockManager.getBlockFamily("coreassets:Plank"), 99);
        inventoryManager.giveItem(player, player, planks);
    }

    private void spawnTestCharacters() {
        Vector3f pos = new Vector3f(0, SURFACE_HEIGHT, 0);
        for (Prefab prefab : prefabManager.listPrefabs(FlexibleMovementComponent.class)) {
            if (prefab.getUrn().getModuleName().toLowerCase().equalsIgnoreCase(MODULE_NAME)) {
                pos.y = prefab.getComponent(CharacterMovementComponent.class).height / 2.0f + SURFACE_HEIGHT;
                EntityRef entity = entityManager.create(prefab, pos);
                entity.send(new CharacterMoveInputEvent(0, 0, 0, Vector3f.zero(), false, false, 1));
                pos.addX(5);
            }
        }
    }

    private void prepareTestbed() {
        paintRegion(air, Region3i.createFromMinMax(
                new Vector3i(0, SURFACE_HEIGHT, 0),
                new Vector3i(20,SURFACE_HEIGHT + 20, 20)
        ));

        paintRegion(ocean, Region3i.createFromMinMax(
                new Vector3i(10, SURFACE_HEIGHT-5, 10),
                new Vector3i(20, SURFACE_HEIGHT, 20)
        ));

        paintRegion(dirt, Region3i.createFromMinMax(
                new Vector3i(0, SURFACE_HEIGHT, 10),
                new Vector3i(10, SURFACE_HEIGHT+5, 20)
        ));
    }

    private void loadBlockTypes() {
        ocean = blockManager.getBlock("coreassets:ocean");
        dirt = blockManager.getBlock("coreassets:dirt");
        air = blockManager.getBlock("engine:air");
    }

    private void paintRegion(Block block, Region3i region) {
        for (Vector3i pos : region) {
            worldProvider.setBlock(pos, block);
        }
    }
}
