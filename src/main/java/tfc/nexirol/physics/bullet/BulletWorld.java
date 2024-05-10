package tfc.nexirol.physics.bullet;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.NativeLibraryLoader;
import tfc.nexirol.physics.wrapper.PhysicsWorld;
import tfc.nexirol.physics.wrapper.RigidBody;
import tfc.nexirol.physics.wrapper.shape.Cube;
import tfc.renirol.util.Pair;

import java.io.File;
import java.util.ArrayList;

public class BulletWorld extends PhysicsWorld {
//    BulletAppState state = new BulletAppState();
//
//    private static final AppStateManager manager = new AppStateManager(new SimpleApplication() {
//        @Override
//        public void simpleInitApp() {
//        }
//    });

    static {
//        NativeLibraryLoader.loadLibbulletjme(true, new File("./"), "Release", "Sp");
        System.loadLibrary("bulletjme");
    }

    PhysicsSpace space;

    public BulletWorld() {
        space = new PhysicsSpace(
                new Vector3f(-1000, -1000, -1000),
                new Vector3f(1000, 1000, 1000),
                PhysicsSpace.BroadphaseType.DBVT,
                64
        );
    }

    @Override
    public void addBody(RigidBody body) {
        PhysicsRigidBody rigid;
        switch (body.collider.type) {
            case CUBE -> {
                BoxCollisionShape collisionShape = new BoxCollisionShape(
                        new Vector3f(
                                (float) (((Cube) body.collider).width / 2f),
                                (float) (((Cube) body.collider).height / 2f),
                                (float) (((Cube) body.collider).length / 2f)
                        )
                );
                rigid = new PhysicsRigidBody(collisionShape, 1.0f);
            }
            default -> throw new RuntimeException("NYI");
        }
        rigid.setPhysicsLocation(new Vector3f(body.vec.x, body.vec.y, body.vec.z));
        rigid.setPhysicsRotation(new Quaternion(-body.quat.x, -body.quat.y, -body.quat.z, body.quat.w));
        if (body.isStatic) rigid.setMass(0.0f);
//        rigid.setCcdMotionThreshold(1f);
        space.add(rigid);
        bodies.add(Pair.of(body, rigid));
    }

    ArrayList<Pair<RigidBody, PhysicsRigidBody>> bodies = new ArrayList<>();

    @Override
    public void tick() {
        Vector3f v3f = new Vector3f();
        Quaternion q = new Quaternion();
        for (Pair<RigidBody, PhysicsRigidBody> body : bodies) {
            body.right().getPhysicsLocation(v3f);
            body.right().getPhysicsRotation(q);
            body.left().setPosition(
                    v3f.x,
                    v3f.y,
                    v3f.z
            );
            body.left().setOrientation(
                    -q.getX(),
                    -q.getY(),
                    -q.getZ(),
                    q.getW()
            );
            body.left().update();
        }
        space.update(0.016f);
    }
}
