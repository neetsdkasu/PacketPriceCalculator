package neetsdkasu.app;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import neetsdkasu.util.NumberUtilities;
import neetsdkasu.util.OptionalDouble;
import neetsdkasu.util.OptionalLong;

public class PacketPriceCalculator extends MIDlet implements CommandListener
{
    MainForm form = null;
    Command cmdExit = new Command("EXIT", Command.EXIT, 1);
    
    public PacketPriceCalculator()
    {
        form = new MainForm();
        form.addCommand(cmdExit);
        Display.getDisplay(this).setCurrent(form);
        form.setCommandListener(this);
    }
    
    protected void startApp() throws MIDletStateChangeException
    {
        
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
        save();
    }
 
    protected void pauseApp()
    {
        
    }
    
    public void commandAction(Command c, Displayable d)
    {
        if (c == cmdExit)
        {
            save();
            notifyDestroyed();
        }
    }
    
    void save()
    {
        SaveData.setPricePerPacket(form.tfPricePerPacket.getString());
        SaveData.setTax(form.tfTax.getString());
        SaveData.close();
    }
    
    class MainForm extends Form implements ItemCommandListener, ItemStateListener
    {
        TextField tfPricePerPacket =
            new TextField(
                "price per packet",
                SaveData.defaultPricePerPacket,
                6,
                TextField.DECIMAL
                );
        
        TextField tfTax =
            new TextField(
                "tax (%)",
                SaveData.defaultTax,
                3,
                TextField.NUMERIC
                );
        
        TextField tfPacketSize =
            new TextField(
                "packet size",
                "0",
                10,
                TextField.NUMERIC
                );

        TextField tfByteSize =
            new TextField(
                "byte size (KB)",
                "0",
                8,
                TextField.DECIMAL
                );
        
        TextField tfPacketPrice =
            new TextField(
                "packet price",
                "0",
                10,
                TextField.DECIMAL
                );
        
        ChoiceGroup cgCalcurationType =
            new ChoiceGroup(
                "calcuration type",
                Choice.EXCLUSIVE,
                new String[]{"by packet(byte)", "by price"},
                null
                );
        
        StringItem siMessage = new StringItem(null, null);
        
        MainForm()
        {
            super("Packet Price Calculator");
            
            if (SaveData.open())
            {
                tfPricePerPacket.setString(SaveData.getPricePerPacket());
                tfTax.setString(SaveData.getTax());
            }
            else
            {
                siMessage.setText("Error Load Storage");
            }
            
            tfPricePerPacket.addCommand(new Command("RESET", Command.ITEM, 1));
            tfTax.addCommand(new Command("RESET", Command.ITEM, 1));
            
            append(siMessage);
            append(new Spacer(240, 2));
            append(tfPacketSize);
            append(new Spacer(240, 2));
            append(tfByteSize);
            append(new Spacer(240, 2));
            append(tfPacketPrice);
            append(new Spacer(240, 2));
            append(cgCalcurationType);
            append(new Spacer(240, 2));
            append(tfPricePerPacket);
            append(new Spacer(240, 2));
            append(tfTax);
    
            setItemStateListener(this);
            tfPricePerPacket.setItemCommandListener(this);
            tfTax.setItemCommandListener(this);
        }
        
        double getPricePerPacket()
        {
            return Math.max(0.0, NumberUtilities.tryParseDouble(tfPricePerPacket.getString()).orElse(0.0));
        }
        
        double getPacketSize()
        {
            return NumberUtilities.tryParseDouble(tfPacketSize.getString()).orElse(0.0);
        }        
        
        double getTax()
        {
            return NumberUtilities.tryParseDouble(tfTax.getString()).orElse(0.0) + 100.0;
        }
        
        double getPacketPrice()
        {
            return Math.max(0.0, NumberUtilities.tryParseDouble(tfPacketPrice.getString()).orElse(0.0));
        }        
        
        void calcByPacket()
        {
            long price = (long)Math.ceil(getPricePerPacket() * getTax() * getPacketSize() * 10.0);
            try
            {
                if (price % 1000L == 0L)
                {
                    tfPacketPrice.setString(Long.toString(price / 1000L));
                }
                else
                {
                    tfPacketPrice.setString(Double.toString((double)price / 1000.0));
                }
            }
            catch (IllegalArgumentException ex)
            {
                tfPacketPrice.setString("0");
                siMessage.setText("Error");
            }
        }
        
        void calcByPrice()
        {
            double ppp = getPricePerPacket();
            if (ppp == 0.0)
            {
                siMessage.setText("Error");
                return;
            }
            long packets = (long)Math.floor(100.0 * getPacketPrice() / (ppp * getTax()));
            try
            {
                tfPacketSize.setString(Long.toString(packets));
            }
            catch (IllegalArgumentException ex)
            {
                tfPacketPrice.setString("0");
                siMessage.setText("Error");
            }
            convertPacketToByte();
        }
        
        boolean convertPacketToByte()
        {
            double packets = getPacketSize();
            long bytes = (long)Math.ceil(packets * 128.0);
            String str = Long.toString(bytes);
            if (bytes == 0L)
            {
                str = "0";
            }
            else if (bytes < 1000L)
            {
                str = "0." + ("000" + str).substring(str.length() + 3 - 3);
            }
            else
            {
                str = str.substring(0, str.length() - 3)
                    + (bytes % 1000L > 0 ? "." + str.substring(str.length() - 3) : "");
            }
            try
            {
                tfByteSize.setString(str);
                return true;
            }
            catch (IllegalArgumentException ex)
            {
                tfByteSize.setString("0");
                siMessage.setText("Error");
                return false;
            }            
        }
        
        boolean convertByteToPacket()
        {
            double bytes = NumberUtilities.tryParseDouble(tfByteSize.getString()).orElse(0.0);
            long packets = (long)Math.ceil(bytes * 1000.0 / 128.0);
            try
            {
                tfPacketSize.setString(Long.toString(packets));
                return true;
            }
            catch (IllegalArgumentException ex)
            {
                tfPacketSize.setString("0");
                siMessage.setText("Error");
                return false;
            }            
        }
        
        boolean isSelectByPacketSize()
        {
            return cgCalcurationType.getSelectedIndex() == 0;
        }
        
        boolean isSelectByPacketPrice()
        {
            return !isSelectByPacketSize();
        }
        
        public void commandAction(Command c, Item item)
        {
            if (item == tfPricePerPacket)
            {
                tfPricePerPacket.setString(SaveData.defaultPricePerPacket);
            }
            else if (item == tfTax)
            {
                tfTax.setString(SaveData.defaultTax);
            }
            switch (cgCalcurationType.getSelectedIndex())
            {
            case 0: // by packet(byte)
                calcByPacket();
                break;
            case 1: // by price
                calcByPrice();
                break;
            }
        }
        
        public void itemStateChanged(Item item)
        {
            if (siMessage.getText() != null)
            {
                siMessage.setText(null);
            }
            boolean recalc = false;
            if (item == cgCalcurationType || item == tfPricePerPacket || item == tfTax)
            {
                recalc = true;
            }
            else if (item == tfPacketSize)
            {
                recalc = convertPacketToByte() && isSelectByPacketSize();
            }
            else if (item == tfByteSize)
            {
                recalc = convertByteToPacket() && isSelectByPacketSize();
            }
            else if (item == tfPacketPrice)
            {
                recalc = isSelectByPacketPrice();
            }
            if (recalc)
            {
                switch (cgCalcurationType.getSelectedIndex())
                {
                case 0: // by packet(byte)
                    calcByPacket();
                    break;
                case 1: // by price
                    calcByPrice();
                    break;
                }
            }
        }
    }
}

class SaveData
{
    static String resName = "savedata";
    static String defaultPricePerPacket = "0.200";
    static String defaultTax = "8";
    static RecordStore res = null;

    static boolean open()
    {
        try
        {
            res = RecordStore.openRecordStore(resName, true);
            if (res.getNumRecords() < 2)
            {
                byte[] buf = defaultPricePerPacket.getBytes();
                res.addRecord(buf, 0, buf.length);
                buf = defaultTax.getBytes();
                res.addRecord(buf, 0, buf.length);
            }
            return true;
        }
        catch (RecordStoreException ex)
        {
            close();
            return false;
        }
    }
    
    static String getPricePerPacket()
    {
        if (res == null)
        {
            return defaultPricePerPacket;
        }
        try
        {
            return new String(res.getRecord(1));
        }
        catch (RecordStoreException ex)
        {
            return defaultPricePerPacket;
        }
    }
    
    static String getTax()
    {
        if (res == null)
        {
            return defaultTax;
        }
        try
        {
            return new String(res.getRecord(2));
        }
        catch (RecordStoreException ex)
        {
            return defaultTax;
        }
    }
    
    static void setPricePerPacket(String ppp)
    {
        if (res == null)
        {
            return;
        }
        byte[] buf = ppp.getBytes();
        try
        {
            res.setRecord(1, buf, 0, buf.length);
        }
        catch (RecordStoreException ex)
        {
        }
    }

    static void setTax(String tax)
    {
        if (res == null)
        {
            return;
        }
        byte[] buf = tax.getBytes();
        try
        {
            res.setRecord(2, buf, 0, buf.length);
        }
        catch (RecordStoreException ex)
        {
        }
    }
    
    static void close()
    {
        if (res == null)
        {
            return;
        }
        try
        {
            res.closeRecordStore();
        }
        catch (RecordStoreException ex)
        {
        }
        res = null;
    }
}