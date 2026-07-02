from __future__ import annotations

import math
import random
from dataclasses import dataclass
from datetime import datetime, timedelta

import pandas as pd
import streamlit as st


@dataclass(frozen=True)
class Reading:
    ts: datetime
    power_w: float
    current_a: float
    voltage_v: float


def make_readings(minutes: int, interval_seconds: int, seed: int) -> list[Reading]:
    rng = random.Random(seed)
    now = datetime.now().replace(microsecond=0)
    points = int(minutes * 60 / interval_seconds)
    start = now - timedelta(seconds=(points - 1) * interval_seconds)
    readings: list[Reading] = []

    for index in range(points):
        ts = start + timedelta(seconds=index * interval_seconds)
        elapsed = index * interval_seconds
        daily_wave = 0.5 + 0.4 * math.sin(elapsed / 1800.0)
        short_wave = 0.08 * math.sin(elapsed / 210.0)
        noise = rng.uniform(-900, 900)

        power_w = max(0.0, 8500 + (daily_wave + short_wave) * 25000 + noise)
        voltage_v = 220 + rng.uniform(-2.5, 2.5)
        current_a = power_w / voltage_v
        readings.append(Reading(ts, power_w, current_a, voltage_v))

    return readings


def to_frame(readings: list[Reading]) -> pd.DataFrame:
    return pd.DataFrame(
        {
            "time": [r.ts for r in readings],
            "power_w": [r.power_w for r in readings],
            "current_a": [r.current_a for r in readings],
            "voltage_v": [r.voltage_v for r in readings],
        }
    )


st.set_page_config(
    page_title="FEMS Demo",
    page_icon="F",
    layout="wide",
)

st.markdown(
    """
    <style>
    .block-container { padding-top: 2rem; }
    div[data-testid="stMetric"] {
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 16px 18px;
        background: #ffffff;
    }
    </style>
    """,
    unsafe_allow_html=True,
)

st.title("FEMS 전력 모니터링 데모")
st.caption("Modbus 장비와 PostgreSQL 없이 Streamlit Cloud에서 실행되는 시뮬레이션 버전")

with st.sidebar:
    st.header("데모 설정")
    minutes = st.slider("조회 범위", min_value=5, max_value=60, value=30, step=5)
    interval_seconds = st.select_slider("측정 간격", options=[5, 10, 30, 60], value=10)
    seed = st.number_input("시뮬레이션 시드", min_value=1, max_value=9999, value=2026)
    st.button("데이터 새로고침", use_container_width=True)

readings = make_readings(minutes, interval_seconds, seed)
df = to_frame(readings)
latest = readings[-1]
previous = readings[-2] if len(readings) > 1 else latest

power_delta = latest.power_w - previous.power_w
current_delta = latest.current_a - previous.current_a
voltage_delta = latest.voltage_v - previous.voltage_v

col1, col2, col3 = st.columns(3)
col1.metric("전력", f"{latest.power_w:,.0f} W", f"{power_delta:,.0f} W")
col2.metric("전류", f"{latest.current_a:,.1f} A", f"{current_delta:,.1f} A")
col3.metric("전압", f"{latest.voltage_v:,.1f} V", f"{voltage_delta:,.1f} V")

chart_df = df.set_index("time")
st.subheader("최근 전력 추이")
st.line_chart(chart_df[["power_w"]], height=360)

left, right = st.columns([2, 1])
with left:
    st.subheader("측정 로그")
    table_df = df.tail(20).sort_values("time", ascending=False).copy()
    table_df["time"] = table_df["time"].dt.strftime("%H:%M:%S")
    table_df["power_w"] = table_df["power_w"].map(lambda value: f"{value:,.0f}")
    table_df["current_a"] = table_df["current_a"].map(lambda value: f"{value:,.1f}")
    table_df["voltage_v"] = table_df["voltage_v"].map(lambda value: f"{value:,.1f}")
    st.dataframe(
        table_df.rename(
            columns={
                "time": "시간",
                "power_w": "전력(W)",
                "current_a": "전류(A)",
                "voltage_v": "전압(V)",
            }
        ),
        use_container_width=True,
        hide_index=True,
    )

with right:
    st.subheader("데모 구성")
    st.write("Spring Boot 원본 프로젝트의 핵심 흐름을 클라우드 데모용으로 단순화했습니다.")
    st.write("- 장비값 수집: Python 시뮬레이션")
    st.write("- 저장소: 메모리 데이터프레임")
    st.write("- 화면: Streamlit 대시보드")
    st.write("- 배포: Streamlit Cloud")
