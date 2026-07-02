# Streamlit Cloud Demo

This project is originally a Spring Boot FEMS sample that reads Modbus data and
stores it in PostgreSQL. For Streamlit Cloud, use the standalone demo package in
`streamlit/` because Streamlit Cloud is not a good fit for running the Spring
server, PostgreSQL, and Modbus simulator together.

## Deploy Settings

- Repository: this GitHub repository
- Branch: `main`
- Main file path: `streamlit/app.py`
- Dependencies file: `streamlit/requirements.txt`
- Streamlit config: `streamlit/.streamlit/config.toml`

## Local Check

```bash
pip install -r streamlit/requirements.txt
streamlit run streamlit/app.py
```

## Demo Scope

- Generates simulated power, current, and voltage readings in Python
- Shows current metrics, a power trend chart, and recent measurement logs
- Does not require Modbus, PostgreSQL, Docker, Maven, or Java
