package mcjty.lostworlds.setup;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModSetup {

    public static Logger logger = null;

    public static Logger getLogger() {
        return logger;
    }


    public void init(FMLCommonSetupEvent e) {
        logger = LogManager.getLogger();
    }
}
