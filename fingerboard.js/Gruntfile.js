module.exports = function(grunt) {
	grunt.initConfig({
		concat: {
			publish: {
				src: [ 'src/constructor.js', 
					'src/dependencies/*.js',
					'src/components/*.js' ],
				dest: 'dist/fingerboard.js'
			},
			play: {
				src: [ 'src/constructor.js', 
					'src/dependencies/*.js',
					'src/components/*.js' ],
				dest: '../public/javascripts/fingerboard.js'
			}
		},
		uglify: {
			publish: {
				files: {
					'dist/fingerboard.min.js': ['dist/fingerboard.js']
				}
			},
			
			play: {
				files: {
					'../public/javascripts/fingerboard.min.js': ['../public/javascripts/fingerboard.js']
				}
			}
		},
		watch:{
			files: ['src/**/*.js'],
			tasks: ['concat:play']
		}
	});
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-watch');

	grunt.registerTask('default', ['concat:publish', 'uglify:publish']);
	grunt.registerTask('play', ['concat:play', 'uglify:play']);
};