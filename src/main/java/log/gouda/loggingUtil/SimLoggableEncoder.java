// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package log.gouda.loggingUtil;

import edu.wpi.first.units.measure.Angle;

/** Add your docs here. */
public class SimLoggableEncoder implements LoggableHardware {
    Angle angle;
    public SimLoggableEncoder() {

    }

    public void setAngle(Angle angle) {
        this.angle = angle;
    }

    @Override
    public void refresh() {}
}
