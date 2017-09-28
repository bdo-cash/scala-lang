/*
 * Copyright (C) 2017-present, Chenai Nakam(chenai.nakam@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hobby.chenai.nakam.basis

import hobby.chenai.nakam.lang.J2S.NonNull
import java.io._

/**
  * @author Chenai Nakam(chenai.nakam@gmail.com)
  * @version 1.0, 28/09/2017
  */
object IO {
  implicit class CloseIn(in: InputStream) {
    def close$(): Unit = {
      if (in.nonNull) try {
        in.close()
      } catch {
        case IOException => _
      }
    }
  }

  implicit class CloseOut(out: OutputStream) {
    def close$(): Unit = {
      flush$.sync$
      if (out.nonNull) try {
        out.close()
      } catch {
        case IOException => _
      }
    }

    def flush$: CloseOut = {
      if (out.nonNull) try {
        out.flush()
      } catch {
        case IOException => _
      }
      this
    }

    def sync$: CloseOut = {
      if (out.nonNull) out match {
        case fo: FileOutputStream =>
          try {
            fo.getFD.sync()
          } catch {
            case SyncFailedException => _
          }
        case _ => _
      }
      this
    }
  }
}
