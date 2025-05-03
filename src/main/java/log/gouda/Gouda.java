// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package log.gouda;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import edu.wpi.first.math.geometry.Ellipse2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rectangle2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.BooleanArrayPublisher;
import edu.wpi.first.networktables.BooleanArraySubscriber;
import edu.wpi.first.networktables.BooleanArrayTopic;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.BooleanSubscriber;
import edu.wpi.first.networktables.BooleanTopic;
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoubleArraySubscriber;
import edu.wpi.first.networktables.DoubleArrayTopic;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.DoubleTopic;
import edu.wpi.first.networktables.FloatArrayPublisher;
import edu.wpi.first.networktables.FloatArraySubscriber;
import edu.wpi.first.networktables.FloatArrayTopic;
import edu.wpi.first.networktables.FloatPublisher;
import edu.wpi.first.networktables.FloatSubscriber;
import edu.wpi.first.networktables.FloatTopic;
import edu.wpi.first.networktables.IntegerArrayPublisher;
import edu.wpi.first.networktables.IntegerArraySubscriber;
import edu.wpi.first.networktables.IntegerArrayTopic;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.IntegerSubscriber;
import edu.wpi.first.networktables.IntegerTopic;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.networktables.StringArraySubscriber;
import edu.wpi.first.networktables.StringArrayTopic;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.networktables.StringTopic;
import edu.wpi.first.networktables.StructArrayPublisher;
import edu.wpi.first.networktables.StructArrayTopic;
import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.networktables.StructTopic;
import edu.wpi.first.networktables.Subscriber;
import edu.wpi.first.units.Measure;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilderImpl;
import log.gouda.loggingUtil.Loggable;

/** Add your docs here. */
public class Gouda {
    private static HashMap<String, Publisher> publisherMap = new HashMap<String, Publisher>();
    private static HashMap<String, Subscriber> subscriberMap = new HashMap<String, Subscriber>();
    private static HashMap<String, SendableBuilder> sendableMap = new HashMap<String, SendableBuilder>();
    private static NetworkTableInstance ntInstance;

    public static void start() {
        ntInstance = NetworkTableInstance.getDefault();
        ntInstance.startServer();
    }

    /**
     * Storing Structs in Network tables
     * @param path the location of the topic in the Network Tables
     * @param value the value to be stored in the Network tables topic
     */
    
    public static void log(String path, Sendable value) {
        NetworkTable table = ntInstance.getTable(path);
        if (sendableMap.containsKey(path)) {
            if (sendableMap.get(path) instanceof SendableBuilderImpl builder) {
                builder.update();
            }
            return;
        }
        SendableBuilderImpl builder = new SendableBuilderImpl();
        builder.setTable(table);
        value.initSendable(builder);
        builder.update();
        builder.startListeners();
        sendableMap.put(path, builder);
    }

    /**
     * Storing Structs in Network tables
     * @param <T>
     * @param path the location of the topic in the Network Tables
     * @param value the value to be stored in the Network tables topic
     * @param struct the type of struct used
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> void log(String path, T value, Struct<T> struct) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof StructPublisher pub) {
                ((StructPublisher<T>) pub).set(value);
            }
            return;
        }
        StructTopic<T> topic = ntInstance.getStructTopic(path, struct);
        StructPublisher<T> pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    /**
     * Storing Struct Arrays in Network tables
     * @param <T>
     * @param path the location of the topic in the Network Tables
     * @param value the value to be stored in the Network tables topic
     * @param struct the type of struct used
     */
    @SuppressWarnings("unchecked")
    public static <T extends StructSerializable> void log(String path, T[] value, Struct<T> struct) {
        if (publisherMap.containsKey(path)) { // containsKey
            if (publisherMap.get(path) instanceof StructArrayPublisher pub) {
                ((StructArrayPublisher<T>) pub).set(value);
                return;
            }
        }
        StructArrayTopic<T> topic = ntInstance.getStructArrayTopic(path, struct);
        StructArrayPublisher<T> pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    /**
     * Storing double values in Network tables
     * 
     */
    public static void log(String path, double value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof DoublePublisher pub) {
                pub.set(value);
            }
            return;
        }
        DoubleTopic topic = ntInstance.getDoubleTopic(path);
        DoublePublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, double[] value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof DoubleArrayPublisher pub) {
                pub.set(value);
            }
            return;
        }
        DoubleArrayTopic topic = ntInstance.getDoubleArrayTopic(path);
        DoubleArrayPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, int value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof IntegerPublisher pub) {
                pub.set(value);
            }
            return;
        }
        IntegerTopic topic = ntInstance.getIntegerTopic(path);
        IntegerPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, int[] value) {
        long[] valueLong = Arrays.stream(value).asLongStream().toArray();
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof IntegerArrayPublisher pub) {
                pub.set(valueLong);
            }
            return;
        }
        IntegerArrayTopic topic = ntInstance.getIntegerArrayTopic(path);
        IntegerArrayPublisher pub = topic.publish();
        pub.set(valueLong);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(valueLong));
    }

    public static void log(String path, boolean value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof BooleanPublisher pub) {
                pub.set(value);
            }
            return;
        }
        BooleanTopic topic = ntInstance.getBooleanTopic(path);
        BooleanPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, boolean[] value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof BooleanArrayPublisher pub) {
                pub.set(value);
            }
            return;
        }
        BooleanArrayTopic topic = ntInstance.getBooleanArrayTopic(path);
        BooleanArrayPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, float value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof FloatPublisher pub) {
                pub.set(value);
            }
            return;
        }
        FloatTopic topic = ntInstance.getFloatTopic(path);
        FloatPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, float[] value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof FloatArrayPublisher pub) {
                pub.set(value);
            }
            return;
        }
        FloatArrayTopic topic = ntInstance.getFloatArrayTopic(path);
        FloatArrayPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, String value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof StringPublisher pub) {
                pub.set(value);
            }
            return;
        }
        StringTopic topic = ntInstance.getStringTopic(path);
        StringPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static void log(String path, String[] value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof StringArrayPublisher pub) {
                pub.set(value);
            }
            return;
        }
        StringArrayTopic topic = ntInstance.getStringArrayTopic(path);
        StringArrayPublisher pub = topic.publish();
        pub.set(value);
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value));
    }

    public static <T extends Enum<T>> void log(String path, T value) {
        if (publisherMap.containsKey(path)) {
            if (publisherMap.get(path) instanceof StringPublisher pub) {
                pub.set(value.toString());
            }
            return;
        }
        StringTopic topic = ntInstance.getStringTopic(path);
        StringPublisher pub = topic.publish();
        pub.set(value.toString());
        publisherMap.put(path, pub);
        subscriberMap.put(path, topic.subscribe(value.toString()));
    }

    public static double getDouble(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof DoubleSubscriber sub) {
                return sub.get();
            } else {
                return 0.000001;
            }
        } else {
            return 0.0000001;
        }
    }

    public static double[] getDoubleArray(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof DoubleArraySubscriber sub) {
                return sub.get();
            } else {
                return new double[] {};
            }
        } else {
            return new double[] {};
        }
    }

    public static int getInteger(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof IntegerSubscriber sub) {
                return ((int) sub.get());
            }
            return -422;
        }
        return -422;
    }

    public static int[] getIntegerArray(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof IntegerArraySubscriber sub) {
                int[] array = Arrays.stream(sub.get()).mapToInt(i -> (int) i).toArray();
                return (array);
            }
            return new int[] {};
        }
        return new int[] {};
    }

    public static boolean getBoolean(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof BooleanSubscriber sub) {
                return sub.get();
            }
            return false;
        }
        return false;
    }

    public static boolean[] getBooleanArray(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof BooleanArraySubscriber sub) {
                return sub.get();
            }
            return new boolean[] {};
        }
        return new boolean[] {};
    }

    public static String getString(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof StringSubscriber sub) {
                return sub.get();
            }
            return "";
        }
        return "";
    }

    public static String[] getStringArray(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof StringArraySubscriber sub) {
                return sub.get();
            }
            return new String[] {};
        }
        return new String[] {};
    }

    public static float getFloat(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof FloatSubscriber sub) {
                return sub.get();
            }
            return Float.valueOf(0);
        }
        return Float.valueOf(0);
    }

    public static float[] getFloatArray(String path) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof FloatArraySubscriber sub) {
                return sub.get();
            }
            return new float[] {};
        }
        return new float[] {};
    }

    public static <T extends Enum<T>> T getEnum(String path, Class<T> valueEnum) {
        if (subscriberMap.containsKey(path)) {
            if (subscriberMap.get(path) instanceof StringSubscriber sub) {
                return Enum.valueOf(valueEnum, sub.get());
            }
            return null;
        }
        return null;
    }
    
    public static void log(String path, Pose2d value) {
        log(path, value, Pose2d.struct);
    }

    public static void log(String path, Pose2d[] value) {
        log(path, value, Pose2d.struct);
    }

    public static void log(String path, Rotation2d value) {
        log(path, value, Rotation2d.struct);
    }

    public static void log(String path, Rotation2d[] value) {
        log(path, value, Rotation2d.struct);
    }

    public static void log(String path, Rotation3d value) {
        log(path, value, Rotation3d.struct);
    }

    public static void log(String path, SwerveModuleState value) {
        log(path, value, SwerveModuleState.struct);
    }

    public static void log(String path, SwerveModuleState[] value) {
        log(path, value, SwerveModuleState.struct);
    }

    public static void log(String path, SwerveModulePosition value) {
        log(path, value, SwerveModulePosition.struct);
    }

    public static void log(String path, SwerveModulePosition[] value) {
        log(path, value, SwerveModulePosition.struct);
    }

    public static void log(String path, Translation2d value) {
        log(path, value, Translation2d.struct);
    }

    public static void log(String path, Translation2d[] value) {
        log(path, value, Translation2d.struct);
    }

    public static void log(String path, Translation3d value) {
        log(path, value, Translation3d.struct);
    }

    public static void log(String path, Translation3d[] value) {
        log(path, value, Translation3d.struct);
    }

    public static void log(String path, ChassisSpeeds value) {
        log(path, value, ChassisSpeeds.struct);
    }

    public static void log(String path, ChassisSpeeds[] value) {
        log(path, value, ChassisSpeeds.struct);
    }

    public static void log(String path, Rectangle2d value) {
        log(path, value, Rectangle2d.struct);
    }

    public static void log(String path, Rectangle2d[] value) {
        log(path, value, Rectangle2d.struct);
    }

    public static void log(String path, Ellipse2d value) {
        log(path, value, Ellipse2d.struct);
    }

    public static void log(String path, Ellipse2d[] value) {
        log(path, value, Ellipse2d.struct);
    }

    public static void process(String path, Loggable loggableClass) {
        Class<?> loggedClass = loggableClass.getClass();
        Field[] fields = loggedClass.getFields();
        for (Field field : fields) {
            try {
                Object value = field.get(loggableClass);
                if (value instanceof Double dvalue) {
                    log(path+"/"+field.getName(), dvalue);
                }
                if (value instanceof double[] dvalue) {
                    log(path+"/"+field.getName(), dvalue);
                }
                if (value instanceof Measure unit) {
                    log(path+"/"+field.getName(), unit.magnitude());
                }
            } catch(IllegalAccessException e) {
                // do nothing
            }
        }
    }
}
