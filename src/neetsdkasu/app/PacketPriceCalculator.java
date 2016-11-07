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
                6,
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