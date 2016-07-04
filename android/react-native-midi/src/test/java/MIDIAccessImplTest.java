import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import android.content.Context;
import com.motiz88.rctmidi.webmidi.impl.MIDIAccessImpl;
import com.motiz88.rctmidi.webmidi.impl.MIDIOptions;
import com.motiz88.rctmidi.webmidi.errors.InvalidAccessError;
import com.motiz88.rctmidi.webmidi.errors.InvalidStateError;
import android.hardware.usb.UsbManager;
import android.bluetooth.BluetoothManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class MIDIAccessImplTest {
  @Mock
  Context mMockContext;
  @Mock
  UsbManager mMockUsbManager;
  @Mock
  BluetoothManager mMockBluetoothManager;
  @Mock
  Resources mMockResources;
  @Mock
  XmlResourceParser xmlEmptyResourceParser;

  @Before
  public void setup() throws XmlPullParserException, IOException {
    when(mMockContext.getApplicationContext())
      .thenReturn(mMockContext);
    when(mMockContext.getSystemService(Context.USB_SERVICE))
      .thenReturn(mMockUsbManager);
    when(mMockContext.getSystemService(Context.BLUETOOTH_SERVICE))
      .thenReturn(mMockBluetoothManager);
    when(mMockContext.getResources())
      .thenReturn(mMockResources);


    when(xmlEmptyResourceParser.getEventType()).thenReturn(XmlResourceParser.COMMENT);
    when(xmlEmptyResourceParser.next()).thenReturn(XmlResourceParser.END_DOCUMENT);

    when(mMockResources.getXml(jp.kshoji.driver.midi.R.xml.device_filter))
      .thenReturn(xmlEmptyResourceParser);
  }

  @Test
  public void MIDIAccessImpl_IsConstructible() throws InvalidStateError, InvalidAccessError {
    assertThat(new MIDIAccessImpl(mMockContext, new MIDIOptions(null)), is(notNullValue()));
  }
}
