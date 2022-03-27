package config;

import utils.LogWriter;

/**
 * This class is simply to have a centrally defined log writer.
 * 
 * @author 190021081
 */
public abstract class Configuration {

    public static final LogWriter LOG = new LogWriter(true);

}
