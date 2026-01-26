module Fastlane
  module Actions
    class ActFetchVersionAction < Action
      def self.find_toml_files(directory)
        Dir.glob("#{directory}/*.toml")
      end
      OUTPUT_PATH = 'gradle/libs.versions.toml'.freeze
      PROPERTIES_FILE = 'gradle.properties'.freeze

      PROPERTY_KEYS = {
        repo: 'VERSION_CATALOG_REPO',
        branch: 'VERSION_CATALOG_BRANCH'
      }.freeze

      SECTIONS = {
        'versions' => ->(k, v) { "#{k} = \"#{v}\"" },
        'libraries' => ->(k, v) { "#{k} = { #{format_dep(v, 'module')} }" },
        'plugins' => ->(k, v) { "#{k} = { #{format_dep(v, 'id')} }" },
        'bundles' => ->(k, v) { "#{k} = [#{v.map { |x| "\"#{x}\"" }.join(', ')}]" }
      }.freeze

      def self.run(params)
        require 'toml-rb'
        require 'tmpdir'

        # Find project root by looking for gradle.properties
        project_root = Dir.pwd
        while project_root != "/" && !File.exist?(File.join(project_root, PROPERTIES_FILE))
          project_root = File.dirname(project_root)
        end

        gradle_props_path = File.join(project_root, PROPERTIES_FILE)
        props = load_properties(gradle_props_path)
        repo = props[PROPERTY_KEYS[:repo]] || UI.user_error!("#{PROPERTY_KEYS[:repo]} not found")
        branch = props[PROPERTY_KEYS[:branch]] || UI.user_error!("#{PROPERTY_KEYS[:branch]} not found")

        Dir.mktmpdir do |tmp_dir|
          unless system('git', 'clone', '--depth=1', '--branch', branch, repo, tmp_dir, [:out, :err] => File::NULL)
            UI.user_error!("Failed to clone repository")
          end

          toml_files = find_toml_files(tmp_dir)
          if toml_files.empty?
            UI.user_error!("No TOML files found in repository")
          end

          merged = toml_files
            .map { |f| TomlRB.load_file(f) }
            .reduce { |a, b| deep_merge(a, b) }

          FileUtils.mkdir_p(File.dirname(OUTPUT_PATH))
          File.write(OUTPUT_PATH, format_toml(merged))

          UI.success("✅ Version catalog saved to #{OUTPUT_PATH}")
        end
      end

      def self.load_properties(file)
        return {} unless File.exist?(file)
        File.readlines(file).each_with_object({}) do |line, hash|
          key, value = line.strip.split('=', 2)
          hash[key.strip] = value.strip if key && value && !line.start_with?('#')
        end
      end

      def self.deep_merge(a, b)
        a.merge(b) { |_, av, bv| av.is_a?(Hash) && bv.is_a?(Hash) ? deep_merge(av, bv) : bv }
      end

      def self.format_toml(data)
        SECTIONS.filter_map do |name, formatter|
          next unless data[name]&.any?
          ["[#{name}]", *data[name].sort.map { |k, v| formatter.call(k, v) }, ""].join("\n")
        end.join("\n")
      end

      def self.format_dep(value, type)
        id = value[type] || "#{value['group']}:#{value['name']}"
        parts = ["#{type} = \"#{id}\""]
        if (v = value['version'])
          parts << (v.is_a?(Hash) ? "version.ref = \"#{v['ref']}\"" : "version = \"#{v}\"")
        end
        parts.join(', ')
      end

      def self.description
        "Fetch and merge version catalog TOML files"
      end

      def self.is_supported?(platform) = true
    end
  end
end