package neetsdkasu.app;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import neetsdkasu.util.NumberUtilities;
import neetsdkasu.util.OptionalDouble;
import neetsdkasu.util.OptionalLong;

public class PacketPriceCalculator extends MIDlet
{
    public PacketPriceCalculator()
    {
        Display.getDisplay(this).setCurrent(new MainForm());
    }
    
    protected void startApp() throws MIDletStateChangeException
    {
        
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
        
    }
 
    protected void pauseApp()
    {
        
    }
    
    class MainForm extends Form implements ItemStateListener
    {
        TextField tfPricePerPacket =
            new TextField(
                "price per packet",
                "0.200",
                10,
                TextField.DECIMAL
                );
        
        TextField tfTax =
            new TextField(
                "tax (%)",
                "8",
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
            append(new Spacer(240, 2));
            append(siMessage);
    
            setItemStateListener(this);
        }
        
        double getPricePerPacket()
        {
            return NumberUtilities.tryParseDouble(tfPricePerPacket.getString()).orElse(0.0);
        }
        
        double getPacketSize()
        {
            return NumberUtilities.tryParseDouble(tfPacketSize.getString()).orElse(0.0);
        }        
        
        double getTax()
        {
            return NumberUtilities.tryParseDouble(tfTax.getString()).orElse(0.0) + 100.0;
        }
        
        void calcByPacket()
        {
            long price = (long)Math.ceil(getPricePerPacket() * getTax() * getPacketSize() / 100.0);
            tfPacketPrice.setString(Long.toString(price));
        }
        
        void calcByPrice()
        {
            
        }
        
        public void itemStateChanged(Item item)
        {
            if (item == cgCalcurationType)
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
            else if (item == tfPacketSize)
            {
                long packets = NumberUtilities.tryParseLong(tfPacketSize.getString()).orElse(0L);
                long bytes = packets * 128L;
                bytes = bytes / 1000L + Math.min(bytes % 1000L, 1L);
                tfByteSize.setString(Long.toString(bytes));
                if (cgCalcurationType.getSelectedIndex() == 0)
                {
                    calcByPacket();
                }
            }
            else if (item == tfByteSize)
            {
                long bytes = NumberUtilities.tryParseLong(tfByteSize.getString()).orElse(0L) * 1000L;
                long packets = bytes / 128L + Math.min(bytes % 128L, 1L);
                tfPacketSize.setString(Long.toString(packets));
                if (cgCalcurationType.getSelectedIndex() == 0)
                {
                    calcByPacket();
                }
            }
        }
    }
}