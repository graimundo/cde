package pt.webdetails.cpf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;


public abstract class PluginSettings {

  protected static Log logger = LogFactory.getLog(PluginSettings.class);
  private static IPluginManager pluginManager;
  
  public abstract String getPluginSystemDir();
  public abstract String getPluginName();
  
  protected static final String SETTINGS_FILE = "settings.xml"; 
  
  private static IPluginManager getPluginManager(){
    if(pluginManager == null){
      pluginManager = PentahoSystem.get(IPluginManager.class);
    }
    return pluginManager;
  }
  
  protected boolean getBooleanSetting(String section, boolean nullValue){
    String setting = getStringSetting(section, null);
    if(setting != null){
      return Boolean.parseBoolean(setting);
    }
    return nullValue;
  }
  
  protected String getStringSetting(String section, String defaultValue){
    return (String) getPluginManager().getPluginSetting(getPluginName(), section, defaultValue);
  }

  
  /**
   * Writes a setting directly to .xml. Does not refresh global config.
   * @param section
   * @param value
   * @return
   */
  protected boolean writeSetting(String section, String value){
    Document settings = null;
    String settingsFilePath = PentahoSystem.getApplicationContext().getSolutionPath("system/" + getPluginSystemDir() + SETTINGS_FILE);
    File settingsFile = new File(settingsFilePath); 
    String nodePath = "settings/" + section;
    
    try {
      settings = XmlDom4JHelper.getDocFromFile(settingsFile, null);// getDocFromFile(settingsFilePath, null);
    } catch (DocumentException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
    if(settings != null){
      Node node = settings.selectSingleNode(nodePath);
      if(node != null){
        node.setText(value);
        FileWriter writer = null;
        try {
          writer = new FileWriter(settingsFile);
          settings.write(writer);
          writer.flush();
          return true;
        } catch (IOException e) {
          logger.error(e);
        }
        finally {
          IOUtils.closeQuietly(writer);
        }
      }
      else {
        logger.error("Couldn't find node");
      }
    }
    else {
      logger.error("Unable to open " + settingsFilePath);
    }
    return false;    
  }

  @SuppressWarnings("unchecked")
  protected List<Element> getSettingsXmlSection(String section) {
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    List<Element> elements = settings.getSystemSettings(getPluginSystemDir() + SETTINGS_FILE, section);
    return elements;
  }
  
}