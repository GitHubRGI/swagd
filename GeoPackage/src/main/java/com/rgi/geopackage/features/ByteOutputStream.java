package com.rgi.geopackage.features;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Luke Lambert
 */
public final class ByteOutputStream implements AutoCloseable
{
    public ByteOutputStream()
    {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public ByteOutputStream(final int capacity)
    {
        if(capacity < 0)
        {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        this.buffer = new byte[capacity];

        this.position = 0;
        this.setByteOrder(DEFAULT_INITIAL_BYTE_ORDER);
    }

    public byte[] array()
    {
        return this.position <= 0 ? EMPTY_ARRAY
                                  : Arrays.copyOfRange(this.buffer, 0, this.position);
    }

    public void write(final byte b)
    {
        this.checkCapacity(BYTE_BYTE_SIZE);

        this.buffer[this.position] = b;

        this.position += BYTE_BYTE_SIZE;
    }

    public void write(final short s)
    {
        this.checkCapacity(SHORT_BYTE_SIZE);

        this.bytePutter.put(s);

        this.position += SHORT_BYTE_SIZE;
    }

    public void write(final int i)
    {
        this.checkCapacity(INT_BYTE_SIZE);

        this.bytePutter.put(i);

        this.position += INT_BYTE_SIZE;
    }

    public void write(final float f)
    {
        this.write(Float.floatToRawIntBits(f));
    }

    public void write(final long l)
    {
        this.checkCapacity(LONG_BYTE_SIZE);

        this.bytePutter.put(l);

        this.position += LONG_BYTE_SIZE;
    }

    public void write(final double d)
    {
        this.write(Double.doubleToLongBits(d));
    }

    public void write(final byte[] bytes)
    {
        if(bytes == null)
        {
            throw new IllegalArgumentException("Bytes may not be null");
        }

        this.checkCapacity(bytes.length);

        System.arraycopy(bytes, 0, this.buffer, this.position, bytes.length);

        this.position += bytes.length;
    }

    public ByteOrder getByteOrder()
    {
        return this.bytePutter.getByteOrder();
    }

    public void setByteOrder(final ByteOrder byteOrder)
    {
        if(byteOrder == null)
        {
            throw new IllegalArgumentException("Byte order may not be null");
        }

        this.bytePutter = Objects.equals(byteOrder, ByteOrder.BIG_ENDIAN) ? this.bigEndianBytePutter
                                                                          : this.littleEndianBytePutter;
    }

    private void checkCapacity(final int requestedBytes)
    {
        if(this.position + requestedBytes > this.buffer.length)
        {
            this.resize();
        }
    }

    private void resize()
    {
        // Growing by 1.5 seems to be the most common strategy:
        // https://groups.google.com/forum/#!msg/comp.lang.c++.moderated/asH_VojWKJw/4jJHJXPzWJ0J
        @SuppressWarnings("NumericCastThatLosesPrecision")
        final byte[] newBuffer = new byte[(int)(this.buffer.length * 1.5)];

        System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);

        this.buffer = newBuffer;
    }

    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte short1(final short x) { return (byte)(x >> 8); } // x >> (1 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte short0(final short x) { return (byte)(x     ); } // x >> (0 * 8)

    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte int3(final int x) { return (byte)(x >> 24); } // x >> (3 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte int2(final int x) { return (byte)(x >> 16); } // x >> (2 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte int1(final int x) { return (byte)(x >>  8); } // x >> (1 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte int0(final int x) { return (byte)(x      ); } // x >> (0 * 8)

    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long7(final long x) { return (byte)(x >> 56); } // x >> (7 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long6(final long x) { return (byte)(x >> 48); } // x >> (6 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long5(final long x) { return (byte)(x >> 40); } // x >> (5 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long4(final long x) { return (byte)(x >> 32); } // x >> (4 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long3(final long x) { return (byte)(x >> 24); } // x >> (3 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long2(final long x) { return (byte)(x >> 16); } // x >> (2 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long1(final long x) { return (byte)(x >>  8); } // x >> (1 * 8)
    @SuppressWarnings({"MagicNumber", "NumericCastThatLosesPrecision"}) private static byte long0(final long x) { return (byte)(x      ); } // x >> (0 * 8)

    private interface BytePutter
    {
        void put(final short s);
        void put(final int   i);
        void put(final long  l);

        ByteOrder getByteOrder();
    }

    private final BytePutter bigEndianBytePutter = new BytePutter()
    {
        @Override
        public void put(final short s)
        {
            ByteOutputStream.this.buffer[ByteOutputStream.this.position    ] = short1(s);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 1] = short0(s);
        }

        @Override
        public void put(final int i)
        {
            ByteOutputStream.this.buffer[ByteOutputStream.this.position    ] = int3(i);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 1] = int2(i);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 2] = int1(i);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 3] = int0(i);
        }

        @Override
        public void put(final long l)
        {
            ByteOutputStream.this.buffer[ByteOutputStream.this.position    ] = long7(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 1] = long6(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 2] = long5(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 3] = long4(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 4] = long3(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 5] = long2(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 6] = long1(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 7] = long0(l);
        }

        @Override
        public ByteOrder getByteOrder()
        {
            return ByteOrder.BIG_ENDIAN;
        }
    };

    private final BytePutter littleEndianBytePutter = new BytePutter()
    {
        @Override
        public void put(final short s)
        {
            ByteOutputStream.this.buffer[ByteOutputStream.this.position    ] = short0(s);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 1] = short1(s);
        }

        @Override
        public void put(final int i)
        {
            ByteOutputStream.this.buffer[ByteOutputStream.this.position    ] = int0(i);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 1] = int1(i);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 2] = int2(i);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 3] = int3(i);
        }

        @Override
        public void put(final long l)
        {
            ByteOutputStream.this.buffer[ByteOutputStream.this.position    ] = long0(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 1] = long1(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 2] = long2(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 3] = long3(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 4] = long4(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 5] = long5(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 6] = long6(l);
            ByteOutputStream.this.buffer[ByteOutputStream.this.position + 7] = long7(l);
        }

        @Override
        public ByteOrder getByteOrder()
        {
            return ByteOrder.LITTLE_ENDIAN;
        }
    };

    private byte[]     buffer;
    private int        position;
    private BytePutter bytePutter;

    private static final ByteOrder DEFAULT_INITIAL_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    private static final int       DEFAULT_INITIAL_CAPACITY   = 32;

    private static final int BYTE_BYTE_SIZE   = 1;
    private static final int SHORT_BYTE_SIZE  = 2;
    private static final int INT_BYTE_SIZE    = 4;
    private static final int FLOAT_BYTE_SIZE  = 4;
    private static final int LONG_BYTE_SIZE   = 8;
    private static final int DOUBLE_BYTE_SIZE = 8;

    private static final byte[] EMPTY_ARRAY = {};

    @Override
    public void close()
    {
        //noinspection AssignmentToNull
        this.buffer = null;
    }
}
