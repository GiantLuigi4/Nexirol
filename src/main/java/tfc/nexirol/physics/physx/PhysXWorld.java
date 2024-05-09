package tfc.nexirol.physics.physx;

import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.extensions.PxRigidBodyExt;
import physx.geometry.PxBoxGeometry;
import physx.physics.*;
import physx.support.PxActorPtr;
import tfc.nexirol.physics.wrapper.PhysicsWorld;
import tfc.nexirol.physics.wrapper.RigidBody;
import tfc.nexirol.physics.wrapper.shape.Cube;
import tfc.renirol.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: how th does one make physx do physics
public class PhysXWorld extends PhysicsWorld {
    static class CustomErrorCallback extends PxErrorCallbackImpl {
        @Override
        public void reportError(PxErrorCodeEnum code, String message, String file, int line) {
            System.out.println(code + ": " + message);
        }
    }

    private static final PxFoundation foundation;
    private static final PxPhysics physics;

    static {
        CustomErrorCallback errorCb = new CustomErrorCallback();
        foundation = PxTopLevelFunctions.CreateFoundation(PxTopLevelFunctions.getPHYSICS_VERSION(), new PxDefaultAllocator(), errorCb);
        physics = PxTopLevelFunctions.CreatePhysics(PxTopLevelFunctions.getPHYSICS_VERSION(), foundation, new PxTolerancesScale());
    }

    final PxScene scene;

    public PhysXWorld() {
        PxSceneDesc sceneDesc = new PxSceneDesc(physics.getTolerancesScale());
        sceneDesc.setCpuDispatcher(PxTopLevelFunctions.DefaultCpuDispatcherCreate(1));
        sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());

        // TODO: cuda
        sceneDesc.getGravity().setY(-9.81f);

        sceneDesc.setKineKineFilteringMode(PxPairFilteringModeEnum.eKEEP);
        sceneDesc.setStaticKineFilteringMode(PxPairFilteringModeEnum.eKEEP);
        PxSimulationEventCallbackImpl cb = new PxSimulationEventCallbackImpl() {
            @Override
            public void onConstraintBreak(PxConstraintInfo constraints, int count) {
                super.onConstraintBreak(constraints, count);
                System.out.println("!");
            }

            @Override
            public void onWake(PxActorPtr actors, int count) {
                super.onWake(actors, count);
                System.out.println("!");
            }

            @Override
            public void onSleep(PxActorPtr actors, int count) {
                super.onSleep(actors, count);
                System.out.println("!");
            }

            @Override
            public void onContact(PxContactPairHeader pairHeader, PxContactPair pairs, int nbPairs) {
                super.onContact(pairHeader, pairs, nbPairs);
                System.out.println("!");
            }

            @Override
            public void onTrigger(PxTriggerPair pairs, int count) {
                super.onTrigger(pairs, count);
                System.out.println("!");
            }
        };
        sceneDesc.setSimulationEventCallback(cb);

        physics.createScene(sceneDesc);

        scene = physics.createScene(sceneDesc);
//        scene.setCCDMaxPasses(128);
//        scene.setCCDMaxSeparation(1f);
    }

    public void addBody(RigidBody body) {
        PxRigidActor actor;
        PxTransform transform = new PxTransform();
        {
            transform.getP().setX(body.vec.x);
            transform.getP().setY(body.vec.y);
            transform.getP().setZ(body.vec.z);
        }
        {
            transform.getQ().setX(body.quat.x);
            transform.getQ().setY(body.quat.y);
            transform.getQ().setZ(body.quat.z);
            transform.getQ().setW(body.quat.w);
        }
        if (body.isStatic) {
            actor = physics.createRigidStatic(transform);
        } else {
            actor = physics.createRigidDynamic(transform);
        }
        PxShape shape;
        PxBoxGeometry geometry;
        PxMaterial material;

        material = physics.createMaterial(
                body.material.staticFriction,
                body.material.dynamicFriction,
                body.material.restitution
        );

        shape = physics.createShape(
                geometry = switch (body.collider.type) {
                    case CUBE -> new PxBoxGeometry(
                            (float) (((Cube) body.collider).width / 2f),
                            (float) (((Cube) body.collider).height / 2f),
                            (float) (((Cube) body.collider).length / 2f)
                    );
                },
                material,
                true
        );
        actor.attachShape(shape);
        if (actor instanceof PxRigidDynamic dynamic) {
            dynamic.setMass(1);
            PxRigidBodyExt.updateMassAndInertia((PxRigidBody) actor, 1.0f);
        }
        body.bindRemove(this, () -> {
            scene.removeActor(actor);
            geometry.destroy();
            material.destroy();
            shape.release();
            actor.release();
        });
        scene.addActor(actor);
        bodies.add(Pair.of(body, actor));
    }

    ArrayList<Pair<RigidBody, PxRigidActor>> bodies = new ArrayList<>();

    public void tick() {
        scene.simulate(0.016f);
        scene.fetchResults(true);
        for (Pair<RigidBody, PxRigidActor> body : bodies) {
            PxVec3 vec3 = body.right().getGlobalPose().getP();
            PxQuat quat = body.right().getGlobalPose().getQ();
            body.left().setPosition(vec3.getX(), vec3.getY(), vec3.getZ());
            body.left().setOrientation(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
            body.left().update();
        }
    }
}
