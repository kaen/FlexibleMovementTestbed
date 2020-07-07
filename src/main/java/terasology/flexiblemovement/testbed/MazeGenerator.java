// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package terasology.flexiblemovement.testbed;

import org.joml.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

public class MazeGenerator {
    public static void generate(WorldProvider worldProvider, Block wall, Block air, int surfaceHeight, int size) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int height = 1;

                // staggered rows of walls
                if ((x % 4 == 1 && y % 4 != 0) || (x % 4 == 3 && (y + 2) % 4 != 0)) {
                    height = 3;
                }

                for (int z = 0; z < height; z++) {
                    worldProvider.setBlock(new Vector3i(x, z + surfaceHeight - 1, y), wall);
                }
            }
        }
    }
}
