"""
Modbus TCP 전력계 시뮬레이터.

진짜 공장 전력계가 없어서, 가짜로 Modbus 슬레이브를 띄워서
전력/전류/전압 값을 1초마다 바꿔준다.
collector가 여기에 접속해서 값을 읽어간다.

레지스터 맵 (holding register):
    0 : 전력 W
    1 : 전류 A  (x10 해서 저장 -> 읽을 때 /10)
    2 : 전압 V  (x10)

* 소수점을 못 보내서(레지스터는 정수만) x10 해서 보내고 받는 쪽에서 나눈다.
  이거 처음에 몰라서 전류가 자꾸 0 나왔었음.
"""
import asyncio
import math
import random
import time

from pymodbus.datastore import (
    ModbusSequentialDataBlock,
    ModbusServerContext,
    ModbusSlaveContext,
)
from pymodbus.server import StartAsyncTcpServer

# holding register 50칸 만들어두고 시작
block = ModbusSequentialDataBlock(0, [0] * 50)
slave = ModbusSlaveContext(hr=block)
context = ModbusServerContext(slaves=slave, single=True)


async def update_values():
    """1초마다 전력값을 바꿔준다. 하루 부하 곡선처럼 보이게 sin 사용."""
    start = time.time()
    while True:
        elapsed = time.time() - start
        # 0~1 사이 왔다갔다 (대충 하루 패턴 흉내)
        load = 0.5 + 0.4 * math.sin(elapsed / 30.0)
        power = 8000 + load * 25000 + random.uniform(-1000, 1000)
        power = max(0, power)

        voltage = 220 + random.uniform(-3, 3)
        # P = V * I 라서 I = P / V (역률은 일단 무시, 나중에 공부)
        current = power / voltage

        slave.setValues(3, 0, [
            int(power),
            int(current * 10),
            int(voltage * 10),
        ])
        await asyncio.sleep(1)


async def main():
    asyncio.create_task(update_values())
    print("Modbus 전력계 시뮬레이터 시작 (포트 5020)")
    await StartAsyncTcpServer(context=context, address=("0.0.0.0", 5020))


if __name__ == "__main__":
    asyncio.run(main())
