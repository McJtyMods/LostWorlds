package mcjty.lostworlds.compat;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostcities.api.ILostSphere;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.function.Function;

public class LostCitiesCompat {

    private static boolean hasLostCities = false;
    private static ILostCities lostCities;

    public static class LostCitiesContext {
        private final ILostCityInformation info;

        public LostCitiesContext(ILostCityInformation info) {
            this.info = info;
        }

        // Get the sphere at x,z. Also check x+15,z and x,z+15 and x+15,z+15
        private ILostSphere findSphere(int x, int z) {
            ILostSphere sphere = info.getSphere(x, z);
            if (sphere == null) {
                sphere = info.getSphere(x + 15, z);
                if (sphere == null) {
                    sphere = info.getSphere(x, z + 15);
                    if (sphere == null) {
                        sphere = info.getSphere(x + 15, z + 15);
                    }
                }
            }
            return sphere;
        }

        // Same as findSphere but all four corners have to be in the sphere
        private ILostSphere findSphereFull(int x, int z) {
            ILostSphere sphere = info.getSphere(x, z);
            if (sphere != null) {
                sphere = info.getSphere(x + 15, z);
                if (sphere != null) {
                    sphere = info.getSphere(x, z + 15);
                    if (sphere != null) {
                        sphere = info.getSphere(x + 15, z + 15);
                    }
                }
            }
            return sphere;
        }

        // Return true if x,z is in a sphere or also x+15,z+15
        public boolean isInSphere(int x, int z) {
            ILostSphere sphere = findSphere(x, z);
            if (sphere != null) {
                return true;
            }
            return false;
        }

        // Return true if x,z is in a sphere and also x+15,z+15
        public boolean isInSphereFull(int x, int z) {
            ILostSphere sphere = findSphereFull(x, z);
            if (sphere != null) {
                return true;
            }
            return false;
        }

        public boolean isInSphere(int x, int y, int z) {
            ILostSphere sphere = info.getSphere(x, y, z);
            if (sphere != null) {
                // Check the actual radius
                BlockPos center = sphere.getCenterPos();
                int dx = x - center.getX();
                int dy = y - center.getY();
                int dz = z - center.getZ();
                float radius = sphere.getRadius()-1;
                if (dx * dx + dy * dy + dz * dz < radius * radius) {
                    return true;
                }
            }
            return false;
        }

        public int getMinSphereY(int x, int z) {
            ILostSphere sphere = findSphere(x, z);
            if (sphere != null) {
                return sphere.getCenterPos().getY() - (int)sphere.getRadius() -1;
            }
            return -4000;
        }

        public int getMaxSphereY(int x, int z) {
            ILostSphere sphere = findSphere(x, z);
            if (sphere != null) {
                return sphere.getCenterPos().getY() + (int)sphere.getRadius() +1;
            }
            return 4000;
        }
    }

    public static void register() {
        if (ModList.get().isLoaded("lostcities")) {
            InterModComms.sendTo(ILostCities.LOSTCITIES, ILostCities.GET_LOST_CITIES, GetLostCities::new);
        }
    }

    public static boolean isHasLostCities() {
        return hasLostCities;
    }

    public static LostCitiesContext getLostCitiesContext(Level level) {
        if (!hasLostCities) {
            return null;
        }
        ILostCityInformation info = lostCities.getLostInfo(level);
        if (info != null) {
            return new LostCitiesContext(info);
        }
        return null;
    }

    public static class GetLostCities implements Function<ILostCities, Void> {
        @Nullable
        @Override
        public Void apply(ILostCities lc) {
            lostCities = lc;
            hasLostCities = true;
            return null;
        }
    }
}
