package com.passer.passwatch.core.ble

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.NameBasedGenerator
import java.util.UUID

/* Type 5 UUIDs
com.passer.passwatch.main.service : 9ce6f88c-0be2-55de-8286-faf078a258fe
com.passer.passwatch.main.characteristic.near_pass_flag : 44270457-8e75-5fef-a4ad-0ab8a86dfb6b
com.passer.passwatch.main.characteristic.time : 068e9c09-8581-5594-b46b-7498315cb915
com.passer.passwatch.main.characteristic.speed : aa53506e-f719-5063-8738-11e01848c73d
com.passer.passwatch.main.characteristic.distance : 7596a38f-1452-5a3d-9d41-c5ca364c4a74
com.passer.passwatch.main.characteristic.video_id : d8747d74-9fde-5583-900d-48b20a105eaf
*/

val uuidGenerator: NameBasedGenerator = Generators.nameBasedGenerator()

enum class UUIDConstants(val uuid: UUID) {
    SERVICE_UUID(uuidGenerator.generate("com.passer.passwatch.main.service")),
    NEAR_PASS_FLAG_CHARACTERISTIC_UUID(uuidGenerator.generate("com.passer.passwatch.main.characteristic.near_pass_flag")),
    TIME_CHARACTERISTIC_UUID(uuidGenerator.generate("com.passer.passwatch.main.characteristic.time")),
    SPEED_CHARACTERISTIC_UUID(uuidGenerator.generate("com.passer.passwatch.main.characteristic.speed")),
    DISTANCE_CHARACTERISTIC_UUID(uuidGenerator.generate("com.passer.passwatch.main.characteristic.distance")),
    VIDEO_ID_CHARACTERISTIC_UUID(uuidGenerator.generate("com.passer.passwatch.main.characteristic.video_id")),
}