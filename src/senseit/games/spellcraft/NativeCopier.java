package senseit.games.spellcraft;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class NativeCopier {


    public static void copy( float[] src, Buffer dst, int numFloats, int offset )
    {
            copyJni( src, dst, numFloats, offset );
            dst.position(0);

            if( dst instanceof ByteBuffer )
                    dst.limit(numFloats << 2);
            else
            if( dst instanceof FloatBuffer )
                    dst.limit(numFloats);
    }

    private native static void copyJni( float[] src, Buffer dst, int numFloats, int offset );
}
