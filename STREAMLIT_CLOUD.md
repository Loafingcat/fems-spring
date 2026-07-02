# Streamlit Cloud Demo

This project is originally a Spring Boot FEMS sample that reads Modbus data and
stores it in PostgreSQL. For Streamlit Cloud, use the standalone demo app in
`streamlit_app.py` because Streamlit Cloud is not a good fit for running the
Spring server, PostgreSQL, and Modbus simulator together.

## Deploy Settings

- Repository: this GitHub repository
- Branch: `main`
- Main file path: `streamlit_app.py`
- Dependencies file: `requirements.txt`

## Local Check

```bash
pip install -r requirements.txt
streamlit run streamlit_app.py
```

## Demo Scope

- Generates simulated power, current, and voltage readings in Python
- Shows current metrics, a power trend chart, and recent measurement logs
- Does not require Modbus, PostgreSQL, Docker, Maven, or Java
