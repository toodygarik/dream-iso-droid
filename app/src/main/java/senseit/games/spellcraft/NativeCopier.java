package senseit.games.spellcraft;

import java.nio.FloatBuffer;

public class NativeCopier {

    public static void copy( float[] src, FloatBuffer dst, int numFloats) {
        dst.put(src, 0, numFloats);
        dst.position(0);

        dst.limit(numFloats);
    }


//    public static void copyOld( float[] src, Buffer dst, int numFloats, int offset )
//    {
//            copyJni( src, dst, numFloats, offset );
//            dst.position(0);
//
//            if( dst instanceof ByteBuffer )
//                    dst.limit(numFloats << 2);
//            else
//            if( dst instanceof FloatBuffer )
//                    dst.limit(numFloats);
//    }
//
//
//    private native static void copyJni( float[] src, Buffer dst, int numFloats, int offset );
}
