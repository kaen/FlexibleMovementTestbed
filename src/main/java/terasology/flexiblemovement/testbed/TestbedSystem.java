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

import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.flexiblemovement.FlexibleMovementComponent;
import org.terasology.flexiblemovement.FlexibleMovementHelper;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
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
    private Block sand;
    private Block ocean;

    @Override
    public void postBegin() {
        super.postBegin();
        loadBlockTypes();
//        spawnTestCharacters();
    }

    @ReceiveEvent(components = InventoryComponent.class)
    public void onPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        inventoryManager.giveItem(player, player, entityManager.create("flexiblemovementtestbed:setTarget"));

        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        EntityRef planks = blockFactory.newInstance(blockManager.getBlockFamily("coreassets:Plank"), 99);
        inventoryManager.giveItem(player, player, planks);
    }

    private void loadBlockTypes() {
        ocean = blockManager.getBlock("coreassets:ocean");
        sand = blockManager.getBlock("coreassets:sand");
        air = blockManager.getBlock("engine:air");
    }


    @Command
    private String fmtSpawnTestCharacters() {
        Vector3f pos = new Vector3f(0, SURFACE_HEIGHT, 0);
        for (Prefab prefab : prefabManager.listPrefabs(FlexibleMovementComponent.class)) {
            if (prefab.getUrn().getModuleName().toLowerCase().equalsIgnoreCase(MODULE_NAME)) {
                pos.y = prefab.getComponent(CharacterMovementComponent.class).height / 2.0f + SURFACE_HEIGHT;
                EntityRef entity = entityManager.create(prefab, pos);
//                entity.send(new CharacterMoveInputEvent(0, 0, 0, new Vector3f(0), false, false, 1));
                pos.add(5, 0, 0);
            }
        }

        return "Spawned test characters";
    }

    @Command
    public String fmtKillAll() {
        for (EntityRef entity : entityManager.getEntitiesWith(FlexibleMovementComponent.class)) {
            entity.destroy();
        }

        return "Killed all entities with FM components";
    }

    @Command
    public String fmtBenchmark() {
        int size = 65;

        fmtKillAll();

        MazeGenerator.generate(worldProvider, sand, air, SURFACE_HEIGHT, size);
        for (int z = 0; z < size; z += 1) {
            EntityRef entity = entityManager.create("flexiblemovementtestbed:landonly", new Vector3f(0, SURFACE_HEIGHT + 1, z));
            FlexibleMovementComponent flexibleMovementComponent = entity.getComponent(FlexibleMovementComponent.class);
            flexibleMovementComponent.setPathGoal(FlexibleMovementHelper.posToBlock(new org.terasology.math.geom.Vector3f(size, SURFACE_HEIGHT + 1, z)));
            flexibleMovementComponent.pathGoalDistance = 0;
        }
        return "";
    }
}
