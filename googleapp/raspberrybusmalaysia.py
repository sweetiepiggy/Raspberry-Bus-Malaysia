import cgi

from google.appengine.api import mail
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app


class MainPage(webapp.RequestHandler):
	def get(self):
		self.response.out.write("""
		<html>
			<body>
				Raspberry Bus Malaysia
			</body>
		</html>""")

class SubmitTrip(webapp.RequestHandler):
	def post(self):
		self.response.out.write('<html><body><pre>')
		self.response.out.write(cgi.escape(self.request.get('content')))
		lines = ''
		for a in self.request.arguments():
			line = a + ': ' + cgi.escape(self.request.get(a))
			self.response.out.write(line)
			lines += line + "\n"
		mail.send_mail(sender="Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>",
				to="Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>",
				subject="Raspberry Bus Malaysia Trip Submission",
				body=lines)
		self.response.out.write('</pre>Sending email ...</body></html>')

application = webapp.WSGIApplication(
		[('/', MainPage),
		('/submit_trip', SubmitTrip)],
		debug=True)

def main():
	run_wsgi_app(application)

if __name__ == "__main__":
	main()

