package tfc.nexirol.physics.physx;

import physx.PxTopLevelFunctions;
import physx.common.*;
import physx.extensions.PxRigidBodyExt;
import physx.geometry.PxBoxGeometry;
import physx.geometry.PxCapsuleGeometry;
import physx.geometry.PxGeometry;
import physx.geometry.PxSphereGeometry;
import physx.physics.*;
import tfc.nexirol.physics.wrapper.PhysicsWorld;
import tfc.nexirol.physics.wrapper.RigidBody;
import tfc.nexirol.physics.wrapper.shape.Capsule;
import tfc.nexirol.physics.wrapper.shape.Cube;
import tfc.nexirol.physics.wrapper.shape.Sphere;
import tfc.renirol.util.Pair;

import java.util.ArrayList;

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
        sceneDesc.setCpuDispatcher(PxTopLevelFunctions.DefaultCpuDispatcherCreate(11));
        sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());

        // TODO: cuda
//        PxCudaContextManagerDesc desc = new PxCudaContextManagerDesc();
//        PxCudaContextManager cudaMgr = PxCudaTopLevelFunctions.CreateCudaContextManager(foundation, desc);
//
//        // Check if CUDA context is valid / CUDA support is available
//        if (cudaMgr != null && cudaMgr.contextIsValid()) {
//            // enable CUDA!
//            sceneDesc.setCudaContextManager(cudaMgr);
//            sceneDesc.setFlags(new PxSceneFlags(PxSceneFlagEnum.eENABLE_GPU_DYNAMICS.value));
//            sceneDesc.setBroadPhaseType(PxBroadPhaseTypeEnum.eGPU);
//
//            // optionally fine tune amount of allocated CUDA memory
//            // PxgDynamicsMemoryConfig memCfg = new PxgDynamicsMemoryConfig();
//            // memCfg.setStuff...
//            // sceneDesc.setGpuDynamicsConfig(memCfg);
//        } else {
//            System.err.println("No CUDA support!");
//        }

        sceneDesc.setDynamicTreeSecondaryPruner(PxDynamicTreeSecondaryPrunerEnum.eBUCKET);
        sceneDesc.setBroadPhaseType(PxBroadPhaseTypeEnum.ePABP);

        sceneDesc.setDynamicBVHBuildStrategy(PxBVHBuildStrategyEnum.eSAH);
        sceneDesc.setStaticBVHBuildStrategy(PxBVHBuildStrategyEnum.eSAH);

        sceneDesc.setSolverType(PxSolverTypeEnum.ePGS);
//        sceneDesc.setSolverBatchSize(sceneDesc.getSolverBatchSize() * 16);
//        sceneDesc.setSolverArticulationBatchSize(sceneDesc.getSolverArticulationBatchSize() * 16);

        sceneDesc.getGravity().setY(-9.81f);

        physics.createScene(sceneDesc);

        scene = physics.createScene(sceneDesc);
    }

//    PxMaterial material = physics.createMaterial(
//            0.5f,
//            0.5f,
//            0f
//    );

    public void addBody(RigidBody body) {
        PxRigidActor actor;
        PxTransform transform = new PxTransform();
        {
            transform.getP().setX(body.vec.x);
            transform.getP().setY(body.vec.y);
            transform.getP().setZ(body.vec.z);
        }
        {
            transform.getQ().setX(-body.quat.x);
            transform.getQ().setY(-body.quat.y);
            transform.getQ().setZ(-body.quat.z);
            transform.getQ().setW(body.quat.w);
        }
        if (body.isStatic) {
            actor = physics.createRigidStatic(transform);
        } else {
            actor = physics.createRigidDynamic(transform);
        }
        PxShape shape;
        PxGeometry geometry;
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
                    case SPHERE -> new PxSphereGeometry((float) ((Sphere) body.collider).radius);
                    case CAPSULE -> new PxCapsuleGeometry(
                            (float) ((Capsule) body.collider).radius,
                            (float) ((Capsule) body.collider).height / 2f
                    );
                    case CONVEX -> throw new RuntimeException("NYI");
                },
                material,
                true,
                new PxShapeFlags(
                        (byte) (PxShapeFlagEnum.eSCENE_QUERY_SHAPE.value | PxShapeFlagEnum.eSIMULATION_SHAPE.value)
                )
        );
        actor.attachShape(shape);
        PxFilterData tmpFilterData = new PxFilterData(1, 1, 0, 0);
        shape.setSimulationFilterData(tmpFilterData);
        if (actor instanceof PxRigidDynamic dynamic) {
            dynamic.setMass(1);
            PxRigidBodyExt.updateMassAndInertia((PxRigidBody) actor, 1.0f);
//            dynamic.setMassSpaceInertiaTensor(new PxVec3(0, 0, 0));
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
        long nt = System.currentTimeMillis();
        scene.simulate(0.016f);
        scene.fetchResults(true);
        long tt = System.currentTimeMillis();
//        System.out.println(1000d / (tt - nt));
        for (Pair<RigidBody, PxRigidActor> body : bodies) {
            PxVec3 vec3 = body.right().getGlobalPose().getP();
            PxQuat quat = body.right().getGlobalPose().getQ();
            body.left().setPosition(vec3.getX(), vec3.getY(), vec3.getZ());
            body.left().setOrientation(-quat.getX(), -quat.getY(), -quat.getZ(), -quat.getW());
            body.left().update();
        }
    }
}
