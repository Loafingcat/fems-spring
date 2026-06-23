package com.limeplus.fems.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import org.springframework.stereotype.Component;

/**
 * Modbus TCP로 전력계(시뮬레이터)에 접속해 레지스터를 읽는 클라이언트.
 *
 * @Component : Spring이 이 클래스를 빈(bean)으로 관리하게 함.
 *              그래야 다른 클래스에서 주입(@Autowired/생성자 주입)받아 쓸 수 있다.
 *
 * Python의 pymodbus AsyncModbusTcpClient 와 같은 역할을 j2mod로 구현한 것.
 * 레지스터 맵은 Python 시뮬레이터와 동일:
 *   0 = 전력 W, 1 = 전류 A(x10), 2 = 전압 V(x10)
 */
@Component
public class ModbusClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5020;
    private static final int REGISTER_START = 0;
    private static final int REGISTER_COUNT = 3;

    private ModbusTCPMaster master;

    /**
     * 전력계에서 레지스터를 읽어 디코딩한 결과를 담는 작은 레코드.
     * record = 자바 16+의 불변 데이터 클래스. 생성자/getter/equals 등이 자동 생성됨.
     */
    public record Reading(double powerW, double currentA, double voltageV) {
    }

    /**
     * 연결을 보장한다. 아직 연결 안 됐으면 새로 연결.
     * Modbus는 연결이 끊길 수 있으므로 읽기 전에 매번 확인한다.
     */
    private synchronized void ensureConnected() throws Exception {
        if (master == null) {
            master = new ModbusTCPMaster(HOST, PORT);
            master.connect();
        }
    }

    /**
     * 전력계에서 holding register 3개를 읽어 물리값으로 변환해 반환.
     *
     * 전류/전압은 시뮬레이터가 x10 해서 정수로 보냈으므로 /10 해서 복원한다.
     * (레지스터는 16비트 정수만 담을 수 있어 소수점을 직접 못 보내기 때문)
     */
    public Reading read() throws Exception {
        ensureConnected();
        InputRegister[] regs = master.readMultipleRegisters(REGISTER_START, REGISTER_COUNT);

        double powerW = regs[0].getValue();
        double currentA = regs[1].getValue() / 10.0;
        double voltageV = regs[2].getValue() / 10.0;

        return new Reading(powerW, currentA, voltageV);
    }
}
