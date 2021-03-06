package org.iota.qupla.abra.context.base;

import org.iota.qupla.abra.block.AbraBlockBranch;
import org.iota.qupla.exception.CodeException;

public abstract class AbraTritCodeBaseContext extends AbraBaseContext
{
  // ASCII code pages, with best encoding for text
  private static final String[] codePage = {
      " ABCDEFGHIJKLMNOPQRSTUVWXYZ",
      ".abcdefghijklmnopqrstuvwxyz",
      "0123456789_$,=+-*/%&|^?:;@#",
      "[]{}()<>!~`'\"\\\r\n\t"
  };
  private static final String[] codePageId = {
      "0",
      "1",
      "-0",
      "-1"
  };

  private static final int[] sizes = new int[300];

  public char[] buffer = new char[32];
  public int bufferOffset;

  public void evalBranchSites(final AbraBlockBranch branch)
  {
    branch.numberSites();

    putInt(branch.inputs.size());
    evalSites(branch.inputs);
    putInt(branch.sites.size());
    putInt(branch.outputs.size());
    putInt(branch.latches.size());
    evalSites(branch.sites);
    evalSites(branch.outputs);
    evalSites(branch.latches);
  }

  public char getChar()
  {
    switch (getTrit())
    {
    case '0':
      return getChar(0);

    case '1':
      return getChar(1);
    }

    switch (getTrit())
    {
    case '0':
      return getChar(2);

    case '1':
      return getChar(3);
    }

    return (char) getInt();
  }

  private char getChar(final int codePageNr)
  {
    int index = 0;
    int power = 1;
    for (int i = 0; i < 3; i++)
    {
      final char trit = getTrit();
      if (trit != '0')
      {
        index += trit == '-' ? -power : power;
      }

      power *= 3;
    }

    return codePage[codePageNr].charAt(index);
  }

  public int getInt()
  {
    int value = 0;
    for (char trit = getTrit(); trit != '0'; trit = getTrit())
    {
      if (trit == '-')
      {
        value <<= 1;
        continue;
      }

      value = (value << 1) | 1;
    }

    return value;
  }

  public String getString()
  {
    if (getTrit() == '0')
    {
      return null;
    }

    final int length = getInt();
    final char[] buffer = new char[length];
    for (int i = 0; i < length; i++)
    {
      buffer[i] = getChar();
    }

    return new String(buffer);
  }

  public char getTrit()
  {
    if (bufferOffset >= buffer.length)
    {
      throw new CodeException("Buffer overflow in getTrit");
    }

    return buffer[bufferOffset++];
  }

  public String getTrits(final int size)
  {
    bufferOffset += size;
    if (bufferOffset > buffer.length)
    {
      throw new CodeException("Buffer overflow in getTrits(" + size + ")");
    }

    return new String(buffer, bufferOffset - size, size);
  }

  public AbraTritCodeBaseContext putChar(final char c)
  {
    // encode ASCII as efficient as possible
    for (int page = 0; page < codePage.length; page++)
    {
      int index = codePage[page].indexOf(c);
      if (index >= 0)
      {
        return putTrits(codePageId[page]).putTrits(lutIndexes[index]);
      }
    }

    return putTrits("--").putInt(c);
  }

  public AbraTritCodeBaseContext putInt(final int value)
  {
    sizes[value < 0 ? 299 : value < 298 ? value : 298]++;

    // binary coded trit value
    int v = value;
    for (; v != 0; v >>= 1)
    {
      putTrit((v & 1) == 0 ? '-' : '1');
    }

    return putTrit('0');

    // here are some possible improvements to reduce size:
    //    // most-used trit value
    //    if (value < 2)
    //    {
    //      return putTrit(value == 0 ? '0' : '1');
    //    }
    //
    //    putTrit('-');
    //
    //    int v = value >> 1;
    //    if (v != 0)
    //    {
    //      v--;
    //      putTrit((v & 1) == 0 ? '-' : '1');
    //      for (v >>= 1; v != 0; v >>= 1)
    //      {
    //        putTrit((v & 1) == 0 ? '-' : '1');
    //      }
    //    }
    //
    //    return putTrit('0');
  }

  public AbraTritCodeBaseContext putString(final String text)
  {
    if (text == null)
    {
      return putTrit('0');
    }

    putTrit('1');
    putInt(text.length());
    for (int i = 0; i < text.length(); i++)
    {
      putChar(text.charAt(i));
    }

    return this;
  }

  public AbraTritCodeBaseContext putTrit(final char trit)
  {
    if (bufferOffset < buffer.length)
    {
      buffer[bufferOffset++] = trit;
      return this;
    }

    // expand buffer
    return putTrits("" + trit);
  }

  public AbraTritCodeBaseContext putTrits(final String trits)
  {
    if (bufferOffset + trits.length() <= buffer.length)
    {
      final char[] copy = trits.toCharArray();
      System.arraycopy(copy, 0, buffer, bufferOffset, copy.length);
      bufferOffset += copy.length;
      return this;
    }

    // double buffer size and try again
    final char[] old = buffer;
    buffer = new char[old.length * 2];
    System.arraycopy(old, 0, buffer, 0, bufferOffset);
    return putTrits(trits);
  }

  @Override
  public String toString()
  {
    // display 40 trit tail end of buffer
    final int start = bufferOffset > 40 ? bufferOffset - 40 : 0;
    return bufferOffset + " " + new String(buffer, start, bufferOffset - start);
  }
}
