FROM 10.160.199.150:8443/library/nginx:1.25
LABEL author="32301163"
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
